package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Decoders;

import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.ProtocolDecoder;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Protocols.Jt808Protocol;
import com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Utils.Jt808Utis;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import io.kaitai.struct.ByteBufferKaitaiStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class Jt808Decoder implements ProtocolDecoder {

    private static final byte START_FLAG = 0x7E;
    private static final DateTimeFormatter BCD_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    @Override
    public boolean supports(byte[] rawData) {
        return rawData != null && rawData.length > 0 && rawData[0] == START_FLAG;
    }

    @Override
    public Mono<TelemetriaVehiculo> decode(byte[] rawData) {
        // 1. VALIDACIÓN FAIL-FAST
        if (rawData == null || rawData.length < 15) {
            log.warn("Trama descartada: Longitud insuficiente ({} bytes)", rawData != null ? rawData.length : 0);
            return Mono.empty();
        }

        return Mono.fromCallable(() -> {
                    try {
                        // Aplicar unescape del protocolo JT808 (0x7d 0x01 -> 0x7d, etc)
                        byte[] cleanData = Jt808Utis.unescape(rawData);
                        ByteBufferKaitaiStream io = new ByteBufferKaitaiStream(cleanData);
                        Jt808Protocol parsedMsg = new Jt808Protocol(io);

                        int msgId = parsedMsg.header().msgId();

                        // Solo nos interesan los reportes de ubicación
                        if (msgId != 0x0200) {
                            return null;
                        }

                        // Validación de integridad del cuerpo
                        if (!(parsedMsg.body() instanceof Jt808Protocol.LocationReport)) {
                            log.warn("Trama 0x0200 con cuerpo incompatible");
                            return null;
                        }

                        Jt808Protocol.LocationReport loc = (Jt808Protocol.LocationReport) parsedMsg.body();
                        String vehicleId = bytesToBcdString(parsedMsg.header().terminalId());

                        // Mapeo al objeto Avro
                        TelemetriaVehiculo.Builder builder = TelemetriaVehiculo.newBuilder()
                                .setVehicleId(vehicleId);

                        // IMPORTANTE: Cast a (int) para asegurar que el bit de signo se respete
                        // antes de la división, evitando coordenadas > 180
                        builder.setLatitude((int)loc.latitudeRaw() / 1000000.0);
                        builder.setLongitude((int)loc.longitudeRaw() / 1000000.0);
                        builder.setSpeed(loc.speedRaw() / 10.0);

                        // Parseo de tiempo BCD
                        try {
                            String timeStr = bytesToBcdString(loc.timeBcd());
                            Instant instant = LocalDateTime.parse(timeStr, BCD_FORMATTER).toInstant(ZoneOffset.UTC);
                            builder.setTimestamp(instant);
                        } catch (Exception e) {
                            log.warn("Fecha corrupta en vehículo {}, usando tiempo actual", vehicleId);
                            builder.setTimestamp(Instant.now());
                        }

                        Map<CharSequence, CharSequence> extras = new HashMap<>();
                        extras.put("statusFlag", String.valueOf(loc.statusFlag()));
                        extras.put("alarmFlag", String.valueOf(loc.alarmFlag()));
                        builder.setAdditionalData(extras);

                        return builder.build();

                    } catch (Exception e) {
                        // Captura silenciosa de cualquier error de Kaitai (Underflow, Validation, etc)
                        log.debug("Omitiendo trama malformada: {}", e.getMessage());
                        return null;
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty) // Si devolvimos null, aquí se convierte en Mono.empty()
                .onErrorResume(e -> Mono.empty()); // Escudo final ante cualquier fallo no previsto
    }

    /**
     * Auxiliar para convertir bytes BCD (Binary Coded Decimal) a String legible.
     */
    private String bytesToBcdString(byte[] bcd) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bcd) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
