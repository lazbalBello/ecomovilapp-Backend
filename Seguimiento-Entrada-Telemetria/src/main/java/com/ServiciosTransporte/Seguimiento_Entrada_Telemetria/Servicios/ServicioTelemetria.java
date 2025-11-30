package com.ServiciosTransporte.Seguimiento_Entrada_Telemetria.Servicios;

import com.ServiciosTransporte.Seguimiento_Entrada_Telemetria.Dtos.TelemetriaJsonDto;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.kafka.core.KafkaTemplate;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ServicioTelemetria {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void processMqttMessage(Message<String> message) {
        String jsonPayload = message.getPayload();
        String topic = (String) message.getHeaders().get("mqtt_receivedTopic");

        try {
            // 1. Deserializar con manejo de errores mejorado
            TelemetriaJsonDto dto = deserializeSafely(jsonPayload, topic);
            if (dto == null) return;

            // 2. Validación mejorada
            if (!isValid(dto, topic)) {
                return;
            }

            // 3. Mapeo a AVRO
            TelemetriaVehiculo avroEvent = mapToAvro(dto, topic);

            // 4. Enviar a Kafka
            kafkaTemplate.send("vehiculos-entrada-telemetria", dto.getVehicleId(), avroEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Error enviando telemetría vehículo {} a Kafka: {}",
                                    dto.getVehicleId(), ex.getMessage());
                            // 👇 Podrías implementar reintentos aquí
                        } else {
                            log.debug("Telemetría enviada - Vehículo: {}, Offset: {}",
                                    dto.getVehicleId(), result.getRecordMetadata().offset());
                        }
                    });

        } catch (Exception e) {
            log.error("Error procesando mensaje MQTT del topic {}: {}", topic, e.getMessage());
        }
    }

    private TelemetriaJsonDto deserializeSafely(String jsonPayload, String topic) {
        try {
            return objectMapper.readValue(jsonPayload, TelemetriaJsonDto.class);
        } catch (Exception e) {
            log.error("JSON inválido en topic {}: {}. Payload: {}",
                    topic, e.getMessage(), jsonPayload);
            return null;
        }
    }

    private boolean isValid(TelemetriaJsonDto dto, String topic) {
        // Validación de vehicleId
        if (dto.getVehicleId() == null || dto.getVehicleId().trim().isEmpty()) {
            String extractedId = extractVehicleIdFromTopic(topic);
            if (extractedId != null) {
                dto.setVehicleId(extractedId);
            } else {
                log.warn("VehicleId no proporcionado en JSON ni en topic: {}", topic);
                return false;
            }
        }

        // Validación de coordenadas básicas
        if (dto.getLatitude() == null && dto.getLongitude() == null) {
            log.warn("Telemetría sin coordenadas para vehículo: {}", dto.getVehicleId());
            // No retornamos false porque podría ser un mensaje de estado sin ubicación
        }

        // Validación de rangos
        if (dto.getLatitude() != null && (dto.getLatitude() < -90 || dto.getLatitude() > 90)) {
            log.warn("Latitud inválida para vehículo {}: {}", dto.getVehicleId(), dto.getLatitude());
            return false;
        }

        if (dto.getLongitude() != null && (dto.getLongitude() < -180 || dto.getLongitude() > 180)) {
            log.warn("Longitud inválida para vehículo {}: {}", dto.getVehicleId(), dto.getLongitude());
            return false;
        }

        return true;
    }

    private TelemetriaVehiculo mapToAvro(TelemetriaJsonDto dto, String topic) {
        return TelemetriaVehiculo.newBuilder()
                .setVehicleId(dto.getVehicleId())
                .setLatitude(dto.getLatitude())
                .setLongitude(dto.getLongitude())
                .setSpeed(dto.getSpeed())
                .setBatteryLevel(dto.getBatteryLevel())
                .setTimestamp(dto.getTimestamp() != null ?
                        Instant.ofEpochMilli(dto.getTimestamp()):Instant.now())
                .setAdditionalData(convertMap(dto.getAdditionalData()))
                .build();
    }

    private Map<CharSequence, CharSequence> convertMap(Map<String, String> originalMap) {
        if (originalMap == null) {
            return null;
        }
        Map<CharSequence, CharSequence> convertedMap = new HashMap<>();
        originalMap.forEach((key, value) -> convertedMap.put(key, value));
        return convertedMap;
    }

    private String extractVehicleIdFromTopic(String topic) {
        if (topic != null && topic.startsWith("vehicles/") && topic.contains("/telemetry")) {
            String[] parts = topic.split("/");
            if (parts.length >= 2) {
                return parts[1];
            }
        }
        return null;
    }
}
