package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Decoders;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.ProtocolDecoder;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Services.GestionVehicleLookupService;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class IrisDecoder implements ProtocolDecoder {

    public static final AttributeKey<String> VEHICLE_ID_KEY = AttributeKey.valueOf("IRIS_VEHICLE_ID");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final GestionVehicleLookupService gestionVehicleLookupService;

    public IrisDecoder() {
        this(null);
    }

    public IrisDecoder(GestionVehicleLookupService gestionVehicleLookupService) {
        this.gestionVehicleLookupService = gestionVehicleLookupService;
        log.info("IRIS decoder activo: soporte 32/80/84 habilitado para IRIS FOLLOW v14.12.03 L2.");
        log.info("IRIS DECODER VERSION 84 ACTIVE");
    }

    @Override
    public boolean supports(byte[] rawData) {
        if (rawData == null || rawData.length < 5 || rawData[0] != '>') {
            log.debug("IrisDecoder.supports(): rawData null/too short or missing leading '>'");
            return false;
        }
        String prefix = new String(rawData, 0, 4, StandardCharsets.US_ASCII);
        boolean result = prefix.equals(">32=") || prefix.equals(">80=") || prefix.equals(">84=");
        log.info("IrisDecoder.supports(): prefix={} result={}", prefix, result);
        return result;
    }

    @Override
    public Mono<TelemetriaVehiculo> decode(byte[] rawData) {
        return decode(rawData, null);
    }

    @Override
    public Mono<TelemetriaVehiculo> decode(byte[] rawData, Channel channel) {
        if (rawData == null || rawData.length < 2) {
            return Mono.empty();
        }

        String data = new String(rawData, StandardCharsets.US_ASCII);

        log.info("IRIS decode: trama={} longitud={} tipo={} ", data, rawData.length, data.startsWith(">") && data.endsWith("<") ? "ASCII framed" : "malformada");

        // Ensure it starts with > and ends with <
        if (!data.startsWith(">") || !data.endsWith("<")) {
            log.warn("Trama IRIS malformada recibida: {}", data);
            return Mono.empty();
        }

        // Remove > and <
        String content = data.substring(1, data.length() - 1);

        if (content.startsWith("32=1")) {
            log.info("IRIS detectado tipo 32: contenido={} ", content);
            return handlePacket32(content, channel);
        } else if (content.startsWith("80=")) {
            log.info("IRIS detectado tipo 80: contenido={} ", content);
            return handlePacket80(content, channel);
        } else if (content.startsWith("84=")) {
            log.info("IRIS detectado tipo 84: contenido={} ", content);
            return handlePacket84(content, channel);
        }

        log.warn("IRIS no reconocido: contenido={} prefijoNoSoportado={}", content, content.length() >= 4 ? content.substring(0, 4) : content);
        return Mono.empty();
    }

    private Mono<TelemetriaVehiculo> handlePacket32(String content, Channel channel) {
        if (channel == null) {
            log.warn("Canal nulo, no se puede guardar el estado para 32.");
            return Mono.empty();
        }
        
        String vehicleId = content.substring(4);
        channel.attr(VEHICLE_ID_KEY).set(vehicleId);
        log.debug("ID de vehículo {} guardado en la conexión.", vehicleId);
        return Mono.empty();
    }

    private Mono<TelemetriaVehiculo> handlePacket80(String content, Channel channel) {
        final String vehicleId = channel != null ? channel.attr(VEHICLE_ID_KEY).get() : null;
        if (vehicleId == null || vehicleId.isEmpty()) {
            log.warn("Paquete 80 recibido sin un vehículo identificado previamente.");
            return Mono.empty();
        }

        Mono<String> vehicleResolution = gestionVehicleLookupService == null
                ? Mono.just(vehicleId)
                : gestionVehicleLookupService.resolveVehicleId(vehicleId)
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("IMEI {} no está asociado a ningún vehículo en Gestion. Se descarta la telemetría.", vehicleId);
                        return Mono.empty();
                    }));

        return vehicleResolution.flatMap(resolvedVehicleId -> buildTelemetryFromPacket80(content, resolvedVehicleId));
    }

    private Mono<TelemetriaVehiculo> handlePacket84(String content, Channel channel) {
        String payload = content.substring(3); // Remove "84="
        String vehicleId = extractVehicleId(payload);
        log.info("Packet 84: payloadRaw='{}' vehicleIdExtraido='{}'", payload, vehicleId);

        if (vehicleId == null || vehicleId.isEmpty()) {
            log.warn("Paquete 84 sin identificador válido: {}", content);
            return Mono.empty();
        }

        String vehiclePayload = payload.substring(vehicleId.length());
        log.info("Packet 84: vehiclePayload='{}'", vehiclePayload);

        Mono<String> vehicleResolution = gestionVehicleLookupService == null
                ? Mono.just(vehicleId)
                : gestionVehicleLookupService.resolveVehicleId(vehicleId)
                    .switchIfEmpty(Mono.defer(() -> {
                        log.warn("ID {} no está asociado a ningún vehículo en Gestion. Se descarta la telemetría.", vehicleId);
                        return Mono.empty();
                    }));

        return vehicleResolution.flatMap(resolvedVehicleId -> buildTelemetryFromPacket84(vehiclePayload, resolvedVehicleId));
    }

    private Mono<TelemetriaVehiculo> buildTelemetryFromPacket80(String content, String vehicleId) {
        String payload = content.substring(3); // Remove "80="

        if (payload.length() < 47) {
            log.warn("Paquete 80 demasiado corto para el vehículo {}. Esperados mínimo 47 caracteres, obtenidos: {} - Payload: {}", vehicleId, payload.length(), payload);
            return Mono.empty();
        }

        try {
            TelemetriaVehiculo.Builder builder = TelemetriaVehiculo.newBuilder();
            builder.setVehicleId(vehicleId);

            String latStr = payload.substring(0, 8);
            String lonStr = payload.substring(8, 17);
            String altStr = payload.substring(17, 21);
            String spdStr = payload.substring(21, 24);
            String headingStr = payload.substring(24, 27);
            String gpsModeStr = payload.substring(27, 28);
            String posAgeStr = payload.substring(28, 29);
            String dateStr = payload.substring(29, 37);
            String timeStr = payload.substring(37, 43);
            String outStr = payload.substring(43, 44);
            String inStr = payload.substring(44, 45);
            String alarmStr = payload.substring(45, 47);

            builder.setLatitude(Double.parseDouble(latStr) / 100000.0);
            builder.setLongitude(Double.parseDouble(lonStr) / 100000.0);
            builder.setSpeed(Double.parseDouble(spdStr));

            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateStr + timeStr, DATE_FORMATTER);
                Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
                builder.setTimestamp(instant);
            } catch (DateTimeParseException e) {
                log.warn("Fecha corrupta en IRIS para vehículo {}, usando actual.", vehicleId);
                builder.setTimestamp(Instant.now());
            }

            Map<CharSequence, CharSequence> extras = new HashMap<>();
            extras.put("altitude", altStr);
            extras.put("heading", headingStr);
            extras.put("gpsMode", gpsModeStr);
            extras.put("positionAge", posAgeStr);
            extras.put("date_literal", dateStr);
            extras.put("time_literal", timeStr);
            extras.put("digitalOutputs", outStr);
            extras.put("digitalInputs", inStr);
            extras.put("lastAlarm", alarmStr);

            builder.setAdditionalData(extras);
            return Mono.just(builder.build());
        } catch (Exception e) {
            log.warn("Error parseando trama 80 para el vehículo {}: {}", vehicleId, e.getMessage());
            return Mono.empty();
        }
    }

    private Mono<TelemetriaVehiculo> buildTelemetryFromPacket84(String payload, String vehicleId) {
        if (payload == null || payload.length() < 17) {
            log.warn("Paquete 84 demasiado corto para el vehículo {}. Payload: {}", vehicleId, payload);
            return Mono.empty();
        }

        try {
            String latStr = payload.substring(0, 8);
            String lonStr = payload.substring(8, 17);

            double latitude = Double.parseDouble(latStr) / 100000.0;
            double longitude = Double.parseDouble(lonStr) / 100000.0;

            log.info("Packet 84 parseado: vehicleId={} latLiteral={} lonLiteral={} lat={} lon={} ", vehicleId, latStr, lonStr, latitude, longitude);

            TelemetriaVehiculo.Builder builder = TelemetriaVehiculo.newBuilder();
            builder.setVehicleId(vehicleId);
            builder.setLatitude(latitude);
            builder.setLongitude(longitude);
            builder.setSpeed(0.0);
            builder.setTimestamp(Instant.now());

            Map<CharSequence, CharSequence> extras = new HashMap<>();
            extras.put("packetType", "84");
            extras.put("rawPayload", payload);
            extras.put("latitudeLiteral", latStr);
            extras.put("longitudeLiteral", lonStr);
            builder.setAdditionalData(extras);
            return Mono.just(builder.build());
        } catch (Exception e) {
            log.warn("Error parseando trama 84 para el vehículo {}. payload={} error={}", vehicleId, payload, e.getMessage());
            return Mono.empty();
        }
    }

    private String extractVehicleId(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        int firstSignIndex = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '+' || c == '-') {
                firstSignIndex = i;
                break;
            }
        }

        if (firstSignIndex <= 0) {
            return null;
        }

        String vehicleId = content.substring(0, firstSignIndex);
        return vehicleId.matches("\\d+") ? vehicleId : null;
    }
}
