package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Kafka.Consumer;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.GpsAuthorizationService;
import com.servicioTransporte.flota.eventos.vehiculo.configuracion.DispositivoGpsAutorizado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GpsAuthorizationConsumerTest {

    @Mock
    private KafkaReceiver<String, DispositivoGpsAutorizado> kafkaReceiver;

    @Mock
    private ReceiverOffset receiverOffset;

    private GpsAuthorizationService authorizationService;
    private GpsAuthorizationConsumer consumer;

    @BeforeEach
    void setUp() {
        authorizationService = new GpsAuthorizationService();
        authorizationService.clear();
        consumer = new GpsAuthorizationConsumer(kafkaReceiver, authorizationService);
    }

    @Test
    @DisplayName("Debe autorizar GPS al recibir evento activo=true")
    void testProcesarEventoActivoTrue() {
        DispositivoGpsAutorizado evento = DispositivoGpsAutorizado.newBuilder()
                .setGpsId("1558885")
                .setMatricula("B123456")
                .setActivo(true)
                .setTimestamp(Instant.now())
                .build();

        ReceiverRecord<String, DispositivoGpsAutorizado> record = mock(ReceiverRecord.class);
        when(record.key()).thenReturn("1558885");
        when(record.value()).thenReturn(evento);
        when(record.receiverOffset()).thenReturn(receiverOffset);

        StepVerifier.create(consumer.procesarRegistro(record))
                .verifyComplete();

        assertTrue(authorizationService.isAuthorized("1558885"));
        verify(receiverOffset).acknowledge();
    }

    @Test
    @DisplayName("Debe revocar GPS al recibir evento activo=false")
    void testProcesarEventoActivoFalse() {
        authorizationService.authorize("1558885");
        assertTrue(authorizationService.isAuthorized("1558885"));

        DispositivoGpsAutorizado evento = DispositivoGpsAutorizado.newBuilder()
                .setGpsId("1558885")
                .setMatricula("B123456")
                .setActivo(false)
                .setTimestamp(Instant.now())
                .build();

        ReceiverRecord<String, DispositivoGpsAutorizado> record = mock(ReceiverRecord.class);
        when(record.key()).thenReturn("1558885");
        when(record.value()).thenReturn(evento);
        when(record.receiverOffset()).thenReturn(receiverOffset);

        StepVerifier.create(consumer.procesarRegistro(record))
                .verifyComplete();

        assertFalse(authorizationService.isAuthorized("1558885"));
        verify(receiverOffset).acknowledge();
    }

    @Test
    @DisplayName("Debe revocar GPS al recibir tombstone (valor nulo)")
    void testProcesarTombstone() {
        authorizationService.authorize("1558885");
        assertTrue(authorizationService.isAuthorized("1558885"));

        ReceiverRecord<String, DispositivoGpsAutorizado> record = mock(ReceiverRecord.class);
        when(record.key()).thenReturn("1558885");
        when(record.value()).thenReturn(null);
        when(record.receiverOffset()).thenReturn(receiverOffset);

        StepVerifier.create(consumer.procesarRegistro(record))
                .verifyComplete();

        assertFalse(authorizationService.isAuthorized("1558885"));
        verify(receiverOffset).acknowledge();
    }
}
