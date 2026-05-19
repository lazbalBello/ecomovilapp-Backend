package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.DecoderFactory;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.ProtocolDecoder;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryIngestionService {

    private final DecoderFactory decoderFactory;
    private final KafkaSender<String, TelemetriaVehiculo> kafkaSender;

    private static final String TOPIC = "vehiculos-entrada-telemetria";

    /**
     * Orquestador principal: Recibe bytes, decodifica, valida y publica.
     */
    public Mono<Void> processRawData(byte[] rawData) {
        log.debug("Iniciando procesamiento de trama de {} bytes", rawData != null ? rawData.length : 0);

        ProtocolDecoder decoder = decoderFactory.getDecoder(rawData);
        if (decoder == null) {
            log.warn("Trama descartada: Protocolo no reconocido o magia de bytes incorrecta");
            return Mono.empty();
        }

        return decoder.decode(rawData)
                .filter(data -> {
                    boolean valid = isDataValid(data);
                    if (!valid) log.debug("Mensaje DESCARTADO por validación de negocio para: {}", data.getVehicleId());
                    return valid;
                })
                .flatMap(data -> publishToKafka(data)
                        // AISLAMIENTO DE KAFKA: Si Kafka está caído, fallamos este mensaje pero mantenemos el servicio TCP arriba
                        .onErrorResume(e -> {
                            log.error("Fallo al publicar en Kafka para el vehículo {}: {}", data.getVehicleId(), e.getMessage());
                            return Mono.empty();
                        })
                )
                // ESCUDO DEL ORQUESTADOR: Garantiza que la tubería TCP siempre reciba un completado (Void), nunca un error de señal
                .onErrorResume(e -> {
                    log.error("Fallo general no controlado en la cadena de ingestión: ", e);
                    return Mono.empty();
                })
                .then();
    }

    /**
     * Validaciones de integridad y límites lógicos.
     * Evita que datos corruptos lleguen a Kafka.
     */
    private boolean isDataValid(TelemetriaVehiculo data) {
        // 1. Identificación básica
        if (data.getVehicleId() == null || data.getVehicleId().toString().isEmpty()) {
            log.debug("Descartado: Sin VehicleId");
            return false;
        }

        // 2. Validación de coordenadas nulas o por defecto (0,0)
        if (data.getLatitude() == null || data.getLongitude() == null ||
                (data.getLatitude() == 0.0 && data.getLongitude() == 0.0)) {
            log.debug("Descartado: Ubicación inválida o (0,0) para vehículo {}", data.getVehicleId());
            return false;
        }

        // 3. Límites lógicos geográficos
        if (Math.abs(data.getLatitude()) > 90.0 || Math.abs(data.getLongitude()) > 180.0) {
            log.warn("Descartado: Coordenadas fuera de límites lógicos para {}", data.getVehicleId());
            return false;
        }

        // 4. Verificación de tiempo
        if (data.getTimestamp() == null) {
            log.debug("Descartado: Mensaje sin timestamp para {}", data.getVehicleId());
            return false;
        }

        return true;
    }

    /**
     * Publicación reactiva en Kafka usando el esquema Avro.
     */
    private Mono<Void> publishToKafka(TelemetriaVehiculo data) {
        // Usamos el vehicleId como KEY para garantizar el orden por vehículo en las particiones de Kafka
        String key = data.getVehicleId().toString();

        SenderRecord<String, TelemetriaVehiculo, String> record = SenderRecord.create(
                TOPIC,
                null, // Partición automática por hash de la clave
                null,
                key,  // Kafka Key (String)
                data, // Payload (Avro)
                key   // Correlation Metadata
        );

        return kafkaSender.send(Mono.just(record))
                .doOnNext(result -> log.debug("Telemetría enviada a Kafka: {} en partición {}",
                        key, result.recordMetadata().partition()))
                .then();
    }
}
