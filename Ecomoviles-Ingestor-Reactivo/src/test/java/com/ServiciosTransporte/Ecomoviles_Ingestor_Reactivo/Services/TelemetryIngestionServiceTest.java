package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.DecoderFactory;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderResult;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryIngestionServiceTest {

    @Mock
    private DecoderFactory decoderFactory;

    @Mock
    private KafkaSender<String, TelemetriaVehiculo> kafkaSender;

    private GpsAuthorizationService authorizationService;
    private TelemetryIngestionService ingestionService;

    @BeforeEach
    void setUp() {
        authorizationService = new GpsAuthorizationService();
        authorizationService.clear();
        ingestionService = new TelemetryIngestionService(decoderFactory, kafkaSender, authorizationService);
    }

    @Test
    @DisplayName("Debe descartar telemetría de vehículo NO autorizado sin enviar a Kafka")
    void testProcessTelemetryUnauthorizedDropped() {
        TelemetriaVehiculo telemetria = TelemetriaVehiculo.newBuilder()
                .setVehicleId("GPS_NO_AUTORIZADO")
                .setLatitude(21.92317)
                .setLongitude(-79.44461)
                .setSpeed(0.0)
                .setTimestamp(Instant.now())
                .setAdditionalData(null)
                .build();

        StepVerifier.create(ingestionService.processTelemetry(telemetria))
                .verifyComplete();

        // Verificar que NUNCA se intentó publicar en Kafka
        verify(kafkaSender, never()).send(any());
    }

    @Test
    @DisplayName("Debe procesar y publicar telemetría cuando el vehículo está autorizado")
    void testProcessTelemetryAuthorizedPublished() {
        String gpsAutorizado = "1558885";
        authorizationService.authorize(gpsAutorizado);

        SenderResult mockResult = mock(SenderResult.class);
        org.apache.kafka.clients.producer.RecordMetadata mockMetadata =
                new org.apache.kafka.clients.producer.RecordMetadata(
                        new org.apache.kafka.common.TopicPartition("vehiculos-entrada-telemetria", 0),
                        0, 0, 0L, 0, 0);
        when(mockResult.recordMetadata()).thenReturn(mockMetadata);
        doReturn(Flux.just(mockResult)).when(kafkaSender).send(any());

        TelemetriaVehiculo telemetria = TelemetriaVehiculo.newBuilder()
                .setVehicleId(gpsAutorizado)
                .setLatitude(21.92317)
                .setLongitude(-79.44461)
                .setSpeed(25.0)
                .setTimestamp(Instant.now())
                .setAdditionalData(null)
                .build();

        StepVerifier.create(ingestionService.processTelemetry(telemetria))
                .verifyComplete();

        // Verificar que SI se publicó en Kafka
        verify(kafkaSender, times(1)).send(any());
    }
}
