package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Decoders;

import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Iris8xxProtocolDecoderTest {

    private Iris8xxProtocolDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new Iris8xxProtocolDecoder();
    }

    @Test
    @DisplayName("supports() debe retornar true para firmas válidas de IRIS y false para JT808 o basura")
    void testSupports() {
        assertTrue(decoder.supports(">84=1558885+2192317-07944461...".getBytes()));
        assertTrue(decoder.supports(">32=1TEST_01<".getBytes()));
        assertTrue(decoder.supports(">80=123".getBytes()));
        assertFalse(decoder.supports(new byte[]{(byte) 0x7E, 0x02, 0x00})); // JT808
        assertFalse(decoder.supports(null));
        assertFalse(decoder.supports(new byte[]{0x01, 0x02}));
    }

    @Test
    @DisplayName("Test 1: Decodificación correcta de trama 84 con coordenadas reales")
    void testDecodePaquete84Exitoso() {
        String rawStr = ">84=1558885+2192317-079444610059000000132007011902191703000000000<";

        Instant expectedApprox = LocalDateTime.now(ZoneId.of("America/Havana")).toInstant(ZoneOffset.UTC);

        StepVerifier.create(decoder.decode(rawStr.getBytes(), null))
                .assertNext(telemetria -> {
                    assertEquals("1558885", telemetria.getVehicleId().toString());
                    assertEquals(21.92317, telemetria.getLatitude(), 0.00001);
                    assertEquals(-79.44461, telemetria.getLongitude(), 0.00001);
                    assertNotNull(telemetria.getTimestamp());
                    long diffSeconds = Math.abs(Duration.between(telemetria.getTimestamp(), expectedApprox).getSeconds());
                    assertTrue(diffSeconds <= 2, "El timestamp debe corresponder a la fecha/hora local de procesamiento en Cuba");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Test 2: Framing de múltiples tramas concatenadas")
    void testExtractFramesMultiplesTramas() {
        String multiStr = ">32=1TEST_01<>84=99999+2192317-079444610059000000132007011902191703000000000<";
        ByteBuf buffer = Unpooled.copiedBuffer(multiStr, StandardCharsets.US_ASCII);

        List<byte[]> frames = decoder.extractFrames(buffer);

        assertEquals(2, frames.size());
        assertEquals(">32=1TEST_01<", new String(frames.get(0), StandardCharsets.US_ASCII));
        assertTrue(new String(frames.get(1), StandardCharsets.US_ASCII).startsWith(">84=99999"));
        assertEquals(0, buffer.readableBytes()); // Buffer completamente procesado
    }

    @Test
    @DisplayName("Test 3: Trama incompleta debe conservarse en el buffer")
    void testExtractFramesTramaIncompleta() {
        String incompleteStr = ">84=1558885+2192317-0794446"; // Sin '<'
        ByteBuf buffer = Unpooled.copiedBuffer(incompleteStr, StandardCharsets.US_ASCII);

        List<byte[]> frames = decoder.extractFrames(buffer);

        assertTrue(frames.isEmpty());
        assertTrue(buffer.readableBytes() > 0); // No debe descartar el buffer si está incompleto
    }

    @Test
    @DisplayName("Test 4: Trama 84 malformada debe retornar Mono.empty() sin excepcionar")
    void testDecodeTramaMalformada() {
        // Latitud con 6 dígitos en vez de 7
        String malformedStr = ">84=1558885+219231-079444610059000000132007011902191703000000000<";

        StepVerifier.create(decoder.decode(malformedStr.getBytes(), null))
                .verifyComplete();
    }

    @Test
    @DisplayName("Test 5: Coordenadas fuera de rango deben descartarse")
    void testDecodeCoordenadasFueraDeRango() {
        // Latitud +91.92317 (Inválida > 90)
        String outOfBoundsStr = ">84=1558885+9192317-079444610059000000132007011902191703000000000<";

        StepVerifier.create(decoder.decode(outOfBoundsStr.getBytes(), null))
                .verifyComplete();
    }

    @Test
    @DisplayName("Test 6: Paquete 32 (Identificación) retorna Mono.empty()")
    void testDecodePaquete32() {
        String paquete32Str = ">32=11558885<";

        StepVerifier.create(decoder.decode(paquete32Str.getBytes(), null))
                .verifyComplete();
    }
}
