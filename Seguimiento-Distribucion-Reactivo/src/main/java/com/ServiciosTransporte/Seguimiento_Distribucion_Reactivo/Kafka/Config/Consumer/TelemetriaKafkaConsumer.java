package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Kafka.Config.Consumer;

import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Servicios.ServicioDistribucion;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetriaKafkaConsumer {

    private final KafkaReceiver<String, TelemetriaVehiculo> kafkaReceiver;
    private final ServicioDistribucion servicioDistribucion;

    @EventListener(ApplicationReadyEvent.class)
    public void iniciarConsumo() {
        kafkaReceiver.receive()
                // Procesar los registros en orden (concatMap preserva el orden de llegada y
                // evita el deadlock que provocaba el combo groupBy/flatMap/publishOn(boundedElastic)).
                // Se acusa recibo (acknowledge -> commit del offset) SOLO después de que el evento
                // haya sido procesado realmente, garantizando que ningún offset avance sin
                // haberse difundido la telemetría.
                .concatMap(record ->
                        servicioDistribucion.procesarTelemetriaKafka(record.value())
                                .onErrorResume(e -> {
                                    // Aislamiento de fallos: si un vehículo falla, no tumba el flujo entero
                                    log.error("Error al procesar evento para vehículo {}: {}", record.key(), e.getMessage());
                                    return Mono.empty();
                                })
                                // Commit dependiente del procesado real
                                .then(Mono.fromRunnable(() -> record.receiverOffset().acknowledge()))
                )
                // Resiliencia total: si ocurre un error fatal de red, se reinicia el flujo
                .doOnError(e -> log.error("Error fatal detectado en la conexión con Kafka", e))
                .retry()
                .subscribe();

        log.info("Consumidor Reactivo de Telemetría iniciado y escuchando...");
    }
}
