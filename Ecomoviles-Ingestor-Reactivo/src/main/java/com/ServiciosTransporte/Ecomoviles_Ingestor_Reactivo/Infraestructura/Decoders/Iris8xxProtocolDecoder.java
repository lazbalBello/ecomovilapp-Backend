package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Decoders;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Exceptions.IrisProtocolException;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.ProtocolDecoder;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.ProtocolFrameDecoder;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Decodificador para el protocolo IRIS (Serie 8xx) que utiliza tramas delimitadas por '>' y '<'.
 * Implementa un parseo de alto rendimiento sin expresiones regulares pesadas.
 */
@Slf4j
@Component
public class Iris8xxProtocolDecoder implements ProtocolDecoder, ProtocolFrameDecoder {

    private static final byte START_DELIMITER = '>'; // 0x3E
    private static final byte END_DELIMITER = '<';   // 0x3C
    private static final int MAX_FRAME_LENGTH = 512; // Límite de seguridad contra tramas corruptas

    private static final String PREFIX_84 = ">84=";
    private static final String PREFIX_80 = ">80=";
    private static final String PREFIX_91 = ">91=";
    private static final String PREFIX_92 = ">92=";
    private static final String PREFIX_32 = ">32=1";

    /** Zona horaria de operación para flota en Cuba */
    private static final ZoneId ZONE_CUBA = ZoneId.of("America/Havana");

    @Override
    public boolean supports(byte[] rawData) {
        if (rawData == null || rawData.length < 4) {
            return false;
        }

        if (rawData[0] != START_DELIMITER) {
            return false;
        }

        String head = new String(rawData, 0, Math.min(rawData.length, 6), StandardCharsets.US_ASCII);
        return head.startsWith(PREFIX_84) ||
               head.startsWith(PREFIX_80) ||
               head.startsWith(PREFIX_91) ||
               head.startsWith(PREFIX_92) ||
               head.startsWith(PREFIX_32);
    }

    @Override
    public List<byte[]> extractFrames(ByteBuf buffer) {
        List<byte[]> frames = new ArrayList<>();

        while (buffer.isReadable()) {
            int startIdx = buffer.indexOf(buffer.readerIndex(), buffer.writerIndex(), START_DELIMITER);

            if (startIdx == -1) {
                // Sin delimitador de inicio '>', descarte de bytes acumulados basura
                buffer.readerIndex(buffer.writerIndex());
                break;
            }

            // Descartar bytes basura anteriores al delimitador '>'
            if (startIdx > buffer.readerIndex()) {
                buffer.readerIndex(startIdx);
            }

            int endIdx = buffer.indexOf(buffer.readerIndex(), buffer.writerIndex(), END_DELIMITER);

            if (endIdx == -1) {
                // Trama incompleta: Verificar protección de tamaño máximo
                if (buffer.readableBytes() > MAX_FRAME_LENGTH) {
                    log.warn("Trama IRIS excede el tamaño máximo permitido ({} bytes) sin cierre '<'. Purgando buffer.", buffer.readableBytes());
                    buffer.readerIndex(buffer.writerIndex());
                }
                break;
            }

            int frameLength = endIdx - buffer.readerIndex() + 1;

            if (frameLength > MAX_FRAME_LENGTH) {
                log.warn("Trama IRIS malformada demasiado larga ({} bytes). Descartando.", frameLength);
                buffer.readerIndex(endIdx + 1);
                continue;
            }

            byte[] frame = new byte[frameLength];
            buffer.readBytes(frame);
            frames.add(frame);
        }

        return frames;
    }

    @Override
    public Mono<TelemetriaVehiculo> decode(byte[] rawData) {
        return decode(rawData, null);
    }

    @Override
    public Mono<TelemetriaVehiculo> decode(byte[] rawData, String channelDeviceId) {
        if (rawData == null || rawData.length == 0) {
            return Mono.empty();
        }

        String frameStr = new String(rawData, StandardCharsets.US_ASCII).trim();

        if (!frameStr.startsWith(">") || !frameStr.endsWith("<")) {
            log.debug("Trama descartada: Delimitadores '>' o '<' no encontrados en trama IRIS.");
            return Mono.empty();
        }

        try {
            if (frameStr.startsWith(PREFIX_84)) {
                return parsePaquete84(frameStr, channelDeviceId);
            } else if (frameStr.startsWith(PREFIX_32)) {
                // Paquete 32 (Identificación de canal): Se maneja a nivel de canal, no genera telemetría directa
                return Mono.empty();
            } else if (frameStr.startsWith(PREFIX_80) || frameStr.startsWith(PREFIX_91) || frameStr.startsWith(PREFIX_92)) {
                log.debug("Mnemónico IRIS no soportado actualmente: {}", frameStr.substring(0, Math.min(frameStr.length(), 5)));
                return Mono.empty();
            } else {
                log.debug("Trama IRIS desconocida omitida: {}", frameStr);
                return Mono.empty();
            }
        } catch (IrisProtocolException e) {
            log.warn("Error al decodificar trama IRIS: {}", e.getMessage());
            return Mono.empty();
        } catch (Exception e) {
            log.error("Fallo inesperado al procesar trama IRIS: {}", e.getMessage(), e);
            return Mono.empty();
        }
    }

    /**
     * Parseo de alto rendimiento para la trama 84 de IRIS.
     * Estructura: >84=ID+LATITUDE-LONGITUDE...<
     * Latitud: 8 caracteres ASCII (signo + 2 grados + 5 decimales)
     * Longitud: 9 caracteres ASCII (signo + 3 grados + 5 decimales)
     */
    private Mono<TelemetriaVehiculo> parsePaquete84(String frameStr, String channelDeviceId) {
        int contentStart = 4; // Después de ">84="
        int contentEnd = frameStr.length() - 1; // Antes del '<'

        if (contentEnd <= contentStart) {
            throw new IrisProtocolException("Cuerpo de trama 84 vacío.");
        }

        // Buscar el primer signo (+ o -) después de ">84=" que marca el inicio de la Latitud
        int signIdx = -1;
        for (int i = contentStart; i < contentEnd; i++) {
            char c = frameStr.charAt(i);
            if (c == '+' || c == '-') {
                signIdx = i;
                break;
            }
        }

        if (signIdx == -1) {
            throw new IrisProtocolException("Trama 84 malformada: No se encontró el signo de la latitud.");
        }

        // 1. Extraer ID del vehículo embebido
        String embeddedId = frameStr.substring(contentStart, signIdx).trim();
        String vehicleId = !embeddedId.isEmpty() ? embeddedId : channelDeviceId;

        if (vehicleId == null || vehicleId.isEmpty()) {
            throw new IrisProtocolException("Trama 84 descartada: Sin ID de vehículo embebido ni en canal.");
        }

        // 2. Comprobar que hay suficientes caracteres para Latitud (8 chars) y Longitud (9 chars)
        if (contentEnd < signIdx + 8 + 9) {
            throw new IrisProtocolException("Trama 84 malformada: Longitud insuficiente para coordenadas.");
        }

        // 3. Parsear Latitud (8 chars: signo + 2 enteros + 5 decimales)
        String latStr = frameStr.substring(signIdx, signIdx + 8);
        double lat = parseCoordinate(latStr, 7, "latitud");

        // 4. Parsear Longitud (9 chars: signo + 3 enteros + 5 decimales)
        int lonSignIdx = signIdx + 8;
        String lonStr = frameStr.substring(lonSignIdx, lonSignIdx + 9);
        double lon = parseCoordinate(lonStr, 8, "longitud");

        // 5. Validar límites geográficos lógicos
        if (Math.abs(lat) > 90.0 || Math.abs(lon) > 180.0) {
            throw new IrisProtocolException(String.format("Coordenadas fuera de rango geográfico para %s: lat=%.5f, lon=%.5f", vehicleId, lat, lon));
        }

        // Fecha/hora en que se procesa la trama, ajustada a la zona horaria de Cuba y convertida a Instant
        Instant processingTimestamp = LocalDateTime.now(ZONE_CUBA).toInstant(ZoneOffset.UTC);

        TelemetriaVehiculo telemetria = TelemetriaVehiculo.newBuilder()
                .setVehicleId(vehicleId)
                .setLatitude(lat)
                .setLongitude(lon)
                .setSpeed(0.0) // No proporcionado en trama 84
                .setTimestamp(processingTimestamp)
                .setAdditionalData(null)
                .build();

        return Mono.just(telemetria);
    }

    /**
     * Convierte una cadena de coordenada fija (ej. +2192317 o -07944461) a double decimal.
     */
    private double parseCoordinate(String coordStr, int expectedDigitsAfterSign, String coordType) {
        char signChar = coordStr.charAt(0);
        if (signChar != '+' && signChar != '-') {
            throw new IrisProtocolException(String.format("Formato de %s inválido: signo no encontrado en '%s'", coordType, coordStr));
        }

        String digitsOnly = coordStr.substring(1);
        if (digitsOnly.length() != expectedDigitsAfterSign) {
            throw new IrisProtocolException(String.format("Formato de %s inválido: se esperaban %d dígitos pero hay %d en '%s'",
                    coordType, expectedDigitsAfterSign, digitsOnly.length(), coordStr));
        }

        try {
            long rawVal = Long.parseLong(digitsOnly);
            double val = rawVal / 100000.0;
            return signChar == '-' ? -val : val;
        } catch (NumberFormatException e) {
            throw new IrisProtocolException(String.format("Error numérico al parsear %s '%s'", coordType, coordStr));
        }
    }
}
