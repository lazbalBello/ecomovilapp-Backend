package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Kafka.Consumer;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.GpsAuthorizationService;
import com.servicioTransporte.flota.eventos.vehiculo.configuracion.DispositivoGpsAutorizado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Consumidor reactivo que sincroniza los eventos de alta/baja de GPS desde Kafka
 * hacia la memoria local (GpsAuthorizationService) para consultas en tiempo real.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class GpsAuthorizationConsumer {

    private final KafkaReceiver<String, DispositivoGpsAutorizado> gpsAuthKafkaReceiver;
    private final GpsAuthorizationService authorizationService;

    @EventListener(ApplicationReadyEvent.class)
    public void iniciarConsumoAutorizaciones() {
        gpsAuthKafkaReceiver.receive()
                .flatMap(this::procesarRegistro)
                .doOnError(e -> log.error("Error en flujo de autorizaciones GPS: {}", e.getMessage(), e))
                .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(2)).maxBackoff(Duration.ofSeconds(30)))
                .subscribe();

        log.info("Consumidor reactivo de autorizaciones GPS iniciado y sincronizando...");
    }

    public Mono<Void> procesarRegistro(ReceiverRecord<String, DispositivoGpsAutorizado> record) {
        return Mono.fromRunnable(() -> {
            try {
                String key = record.key();
                DispositivoGpsAutorizado valor = record.value();

                if (valor == null) {
                    // Tombstone de compactación: clave eliminada
                    if (key != null) {
                        authorizationService.revoke(key);
                    }
                } else {
                    String gpsId = valor.getGpsId() != null ? valor.getGpsId().toString() : key;
                    if (Boolean.TRUE.equals(valor.getActivo())) {
                        authorizationService.authorize(gpsId);
                    } else {
                        authorizationService.revoke(gpsId);
                    }
                }
            } finally {
                record.receiverOffset().acknowledge();
            }
        }).then();
    }
}
