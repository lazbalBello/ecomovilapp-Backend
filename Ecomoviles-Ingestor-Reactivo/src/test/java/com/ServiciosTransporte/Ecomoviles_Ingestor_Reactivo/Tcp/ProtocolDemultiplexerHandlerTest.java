package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Tcp;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.DecoderFactory;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Decoders.Iris8xxProtocolDecoder;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Decoders.Jt808Decoder;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.TelemetryIngestionService;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProtocolDemultiplexerHandlerTest {

    private DecoderFactory decoderFactory;
    private TelemetryIngestionService ingestionService;
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        Iris8xxProtocolDecoder irisDecoder = new Iris8xxProtocolDecoder();
        Jt808Decoder jt808Decoder = new Jt808Decoder();
        decoderFactory = new DecoderFactory(List.of(irisDecoder, jt808Decoder));

        ingestionService = mock(TelemetryIngestionService.class);
        when(ingestionService.processTelemetry(any())).thenReturn(Mono.empty());
        when(ingestionService.processRawData(any())).thenReturn(Mono.empty());

        ProtocolDemultiplexerHandler handler = new ProtocolDemultiplexerHandler(decoderFactory, ingestionService);
        channel = new EmbeddedChannel(handler);
    }

    @Test
    @DisplayName("Demultiplexor debe procesar tramas IRIS 84 y pasarlas a processTelemetry")
    void testProcesarTramaIris() {
        String tramaIris = ">84=1558885+2192317-079444610059000000132007011902191703000000000<";
        channel.writeInbound(Unpooled.copiedBuffer(tramaIris, StandardCharsets.US_ASCII));

        ArgumentCaptor<TelemetriaVehiculo> captor = ArgumentCaptor.forClass(TelemetriaVehiculo.class);
        verify(ingestionService, timeout(1000).atLeastOnce()).processTelemetry(captor.capture());

        TelemetriaVehiculo telemetria = captor.getValue();
        assertEquals("1558885", telemetria.getVehicleId().toString());
        assertEquals(21.92317, telemetria.getLatitude(), 0.00001);
        assertEquals(-79.44461, telemetria.getLongitude(), 0.00001);
    }

    @Test
    @DisplayName("No regresión: Demultiplexor debe pasar tramas binarias JT808 a processRawData sin alteración")
    void testProcesarTramaJt808() {
        byte[] rawJt808 = new byte[]{(byte) 0x7E, (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x01, (byte) 0x23, (byte) 0x45, (byte) 0x67, (byte) 0x89, (byte) 0x01, (byte) 0x00, (byte) 0x7E};
        channel.writeInbound(Unpooled.copiedBuffer(rawJt808));

        verify(ingestionService, timeout(1000).atLeastOnce()).processRawData(any());
    }
}
