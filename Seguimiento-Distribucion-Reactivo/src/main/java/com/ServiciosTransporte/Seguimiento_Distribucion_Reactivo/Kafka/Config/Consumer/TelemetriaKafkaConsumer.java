package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Kafka.Config.Consumer;

import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Servicios.ServicioDistribucion;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelemetriaKafkaConsumer {

    private final KafkaReceiver<String, TelemetriaVehiculo> kafkaReceiver;
    private final ServicioDistribucion servicioDistribucion;

    @EventListener(ApplicationReadyEvent.class)
    public void iniciarConsumo() {
        kafkaReceiver.receive()
                // 1. Aislamiento: Agrupar por partición para mantener el orden exacto de Kafka
                .groupBy(m -> m.receiverOffset().topicPartition())
                .flatMap(partitionFlux -> partitionFlux
                        // Procesar cada partición en hilos separados
                        .publishOn(Schedulers.boundedElastic())

                        // 2. Micro-Batching: Recolectar hasta 200 mensajes o esperar 500ms
                        .bufferTimeout(200, Duration.ofMillis(500))

                        // ConcatMap asegura que el lote actual se procese antes de pasar al siguiente en la misma partición
                        .concatMap(lote -> {
                            // 3. DEDUPLICACIÓN: Sobrescribe la clave con el mensaje más reciente
                            Map<String, ReceiverRecord<String, TelemetriaVehiculo>> ultimosPorVehiculo = new LinkedHashMap<>();
                            for (ReceiverRecord<String, TelemetriaVehiculo> record : lote) {
                                ultimosPorVehiculo.put(record.key(), record);
                            }

                            if (lote.size() > ultimosPorVehiculo.size()) {
                                log.debug("Lote optimizado: de {} eventos entrantes, se procesarán {} únicos.",
                                        lote.size(), ultimosPorVehiculo.size());
                            }

                            // 4. Procesar solo los mensajes filtrados
                            return Flux.fromIterable(ultimosPorVehiculo.values())
                                    .flatMap(record -> servicioDistribucion.procesarTelemetriaKafka(record.value())
                                            .onErrorResume(e -> {
                                                // Aislamiento de fallos: Si un vehículo falla, no tumba el lote entero
                                                log.error("Error al procesar evento para vehículo {}: {}", record.key(), e.getMessage());
                                                return Mono.empty();
                                            })
                                    )
                                    // 5. Commit asíncrono manual del offset más alto de la partición procesada
                                    .then(Mono.defer(() -> {
                                        ReceiverRecord<String, TelemetriaVehiculo> ultimoRegistroLote = lote.get(lote.size() - 1);
                                        ultimoRegistroLote.receiverOffset().acknowledge();
                                        return Mono.empty();
                                    }));
                        })
                )
                // 6. Resiliencia total: Si ocurre un error fatal en la red, se reinicia el flujo
                .doOnError(e -> log.error("Error fatal detectado en la conexión con Kafka", e))
                .retry()
                .subscribe();

        log.info("Consumidor Reactivo de Telemetría iniciado y escuchando...");
    }
}
