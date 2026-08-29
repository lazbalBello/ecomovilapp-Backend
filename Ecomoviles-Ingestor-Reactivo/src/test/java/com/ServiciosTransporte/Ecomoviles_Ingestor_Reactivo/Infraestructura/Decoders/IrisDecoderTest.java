package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Decoders;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.GestionVehicleLookupService;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class IrisDecoderTest {

    private static final class StubGestionVehicleLookupService extends GestionVehicleLookupService {
        StubGestionVehicleLookupService() {
            super("http://localhost:8080");
        }

        @Override
        public Mono<String> resolveVehicleId(String imei) {
            if ("987654321012345".equals(imei) || "ABC123".equals(imei) || "1558885".equals(imei)) {
                return Mono.just(imei);
            }
            return Mono.empty();
        }
    }

    private IrisDecoder irisDecoder;
    private Channel channel;

    @BeforeEach
    void setUp() {
        irisDecoder = new IrisDecoder(new StubGestionVehicleLookupService());
        channel = new EmbeddedChannel();
    }

    @Test
    void testSupports() {
        assertTrue(irisDecoder.supports(">32=1ABC<".getBytes(StandardCharsets.US_ASCII)));
        assertTrue(irisDecoder.supports(">80=123<".getBytes(StandardCharsets.US_ASCII)));
        assertTrue(irisDecoder.supports(">84=1558885+2192306-07944479<".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(irisDecoder.supports(new byte[]{0x7E, 0x01, 0x02})); // JT808
        assertFalse(irisDecoder.supports(new byte[]{}));
    }

    @Test
    void testPacket84WithRealIrisFrame() {
        String packet = ">84=1558885+2192306-079444790042000289132007011301260503000000000<";
        Mono<TelemetriaVehiculo> result = irisDecoder.decode(packet.getBytes(StandardCharsets.US_ASCII), channel);

        StepVerifier.create(result)
                .assertNext(telemetria -> {
                    assertEquals("1558885", telemetria.getVehicleId());
                    assertEquals(21.92306, telemetria.getLatitude(), 0.000001);
                    assertEquals(-79.44479, telemetria.getLongitude(), 0.000001);
                    assertNotNull(telemetria.getTimestamp());
                })
                .verifyComplete();
    }

    @Test
    void testPacket84WithZeroCoordinatesFrame() {
        String packet = ">84=1558885+0000000+00000000000000000001200001010000000300000000A<";
        Mono<TelemetriaVehiculo> result = irisDecoder.decode(packet.getBytes(StandardCharsets.US_ASCII), channel);

        StepVerifier.create(result)
                .assertNext(telemetria -> {
                    assertEquals("1558885", telemetria.getVehicleId());
                    assertEquals(0.0, telemetria.getLatitude(), 0.000001);
                    assertEquals(0.0, telemetria.getLongitude(), 0.000001);
                    assertNotNull(telemetria.getTimestamp());
                })
                .verifyComplete();
    }

    @Test
    void testPacket32SetsVehicleId() {
        String packet = ">32=1ABC123<";
        Mono<TelemetriaVehiculo> result = irisDecoder.decode(packet.getBytes(StandardCharsets.US_ASCII), channel);
        
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals("ABC123", channel.attr(IrisDecoder.VEHICLE_ID_KEY).get());
    }

    @Test
    void testPacket80WithFullExample() {
        // Establecer el ID primero
        channel.attr(IrisDecoder.VEHICLE_ID_KEY).set("ABC123");

        String packet = ">80=+4048128-0037032907770000001220040116094239AA21<";
        Mono<TelemetriaVehiculo> result = irisDecoder.decode(packet.getBytes(StandardCharsets.US_ASCII), channel);
        
        StepVerifier.create(result)
                .assertNext(telemetria -> {
                    assertEquals("ABC123", telemetria.getVehicleId());
                    assertEquals(40.48128, telemetria.getLatitude()); // El manual dice 40.48128 
                    assertEquals(-3.70329, telemetria.getLongitude());
                    assertEquals(0.0, telemetria.getSpeed());
                    assertNotNull(telemetria.getTimestamp());
                    
                    assertEquals("0777", telemetria.getAdditionalData().get("altitude"));
                    assertEquals("000", telemetria.getAdditionalData().get("heading"));
                    assertEquals("1", telemetria.getAdditionalData().get("gpsMode"));
                    assertEquals("2", telemetria.getAdditionalData().get("positionAge"));
                    assertEquals("A", telemetria.getAdditionalData().get("digitalOutputs"));
                    assertEquals("A", telemetria.getAdditionalData().get("digitalInputs"));
                    assertEquals("21", telemetria.getAdditionalData().get("lastAlarm"));
                    assertEquals("20040116", telemetria.getAdditionalData().get("date_literal"));
                    assertEquals("094239", telemetria.getAdditionalData().get("time_literal"));
                })
                .verifyComplete();
    }

    @Test
    void testPacket80WithoutSessionId() {
        String packet = ">80=+4048128-0037032907770000001220040116094239AA21<";
        Mono<TelemetriaVehiculo> result = irisDecoder.decode(packet.getBytes(StandardCharsets.US_ASCII), channel);
        
        StepVerifier.create(result)
                .verifyComplete(); // Debe retornar empty y loguear warning, sin lanzar excepción
    }

    @Test
    void testMalformedPacket() {
        String packet = ">80=CORTP<";
        channel.attr(IrisDecoder.VEHICLE_ID_KEY).set("ABC123");
        
        Mono<TelemetriaVehiculo> result = irisDecoder.decode(packet.getBytes(StandardCharsets.US_ASCII), channel);
        
        StepVerifier.create(result)
                .verifyComplete(); // Retorna empty
    }

    @Test
    void testPacket80RequiresKnownImeiFromGestion() {
        String imei = "987654321012345";
        channel.attr(IrisDecoder.VEHICLE_ID_KEY).set(imei);

        String packet = ">80=+4048128-0037032907770000001220040116094239AA21<";
        Mono<TelemetriaVehiculo> result = irisDecoder.decode(packet.getBytes(StandardCharsets.US_ASCII), channel);

        StepVerifier.create(result)
                .assertNext(telemetria -> assertEquals(imei, telemetria.getVehicleId()))
                .verifyComplete();
    }
}
