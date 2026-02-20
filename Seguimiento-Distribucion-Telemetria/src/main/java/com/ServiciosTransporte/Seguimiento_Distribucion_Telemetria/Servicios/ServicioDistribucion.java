package com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Servicios;

import com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Dtos.EstadoCirculacion;
import com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Dtos.EstadoDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Dtos.VehiculoRedisDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Mappers.TelemetriaMapper;
import com.ServiciosTransporte.Seguimiento_Distribucion_Telemetria.Utils.GeoUtils;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioDistribucion {

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TelemetriaMapper telemetriaMapper;
    private static final String ESTADO_DEFECTO = EstadoCirculacion.ACTIVO.name();
    private static final double UMBRAL_MOVIMIENTO_METROS = 10.0;
    private static final long TIEMPO_MINIMO_ENVIO_MS = 2000;
    private final Map<String, Long> ultimaActualizacionWS = new ConcurrentHashMap<>();

    @KafkaListener(topics = "vehiculos-entrada-telemetria", containerFactory = "kafkaListenerContainerFactory")
    public void procesarLoteTelemetria(List<TelemetriaVehiculo> eventosRaw) {

        // Log de entrada para verificar que Kafka está consumiendo
        log.info("📥 Kafka: Recibido lote de {} eventos.", eventosRaw.size());

        Map<String, TelemetriaVehiculo> eventosUnicos = eventosRaw.stream()
                .collect(Collectors.toMap(
                        e -> e.getVehicleId().toString(),
                        e-> e,
                        (oldV, newV) -> newV
                ));
        List<TelemetriaVehiculo> eventosProcesar = new ArrayList<>(eventosUnicos.values());

        List<Object> estadosRedis = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (TelemetriaVehiculo ev : eventosProcesar) {
                byte[] key = ("vehiculo:" + ev.getVehicleId()).getBytes(StandardCharsets.UTF_8);
                connection.hMGet(key, "lat".getBytes(), "lon".getBytes(), "status".getBytes());
            }
            return null;
        });

        List<VehiculoRedisDto> paraEnviarWebsocket = new ArrayList<>();
        List<TelemetriaVehiculo> paraGuardarRedis = new ArrayList<>();
        long ahora = System.currentTimeMillis();

        for (int i = 0; i < eventosProcesar.size(); i++) {
            TelemetriaVehiculo evento = eventosProcesar.get(i);
            List<Object> redisResult = (List<Object>) estadosRedis.get(i);
            boolean actualizar = false;
            boolean pasoElThrottling = false;
            EstadoCirculacion estadoActual = EstadoCirculacion.ACTIVO;
            Double newLat = evento.getLatitude();
            Double newLon = evento.getLongitude();
            boolean eventoTieneUbicacion = newLat != null && newLon != null;

            // Verificar si tenemos datos previos en Redis
            if (redisResult != null && !redisResult.isEmpty() && redisResult.get(0) != null) {
                try {
                    String latStr = safeConvertToString(redisResult.get(0));
                    String lonStr = safeConvertToString(redisResult.get(1));
                    String statusStr = safeConvertToString(redisResult.get(2));

                    estadoActual = statusStr != null ? EstadoCirculacion.valueOf(statusStr) : EstadoCirculacion.ACTIVO;

                    if (eventoTieneUbicacion && latStr != null && lonStr != null) {
                        double latAnt = Double.parseDouble(latStr);
                        double lonAnt = Double.parseDouble(lonStr);
                        double distancia = GeoUtils.calcularDistanciaMetros(latAnt, lonAnt, newLat, newLon);

                        if (distancia >= UMBRAL_MOVIMIENTO_METROS) {
                            actualizar = true;
                            log.debug("Vehículo {} se movió {}m. Actualizando.", evento.getVehicleId(), distancia);
                        } else {
                             log.debug("Vehículo {} movimiento insignificante ({}m). Filtrado.", evento.getVehicleId(), distancia);
                        }
                    } else {
                        actualizar = true; // Si faltan datos previos o actuales, actualizamos por seguridad
                    }

                } catch (Exception e) {
                    log.error("Error procesando lógica Redis para {}: {}", evento.getVehicleId(), e.getMessage());
                    actualizar = true; // Ante error, forzamos actualización
                }
            } else {
                actualizar = true; // Vehículo nuevo en Redis
            }

            if (actualizar) {
                long ultimoEnvio = ultimaActualizacionWS.getOrDefault(evento.getVehicleId().toString(), 0L);

                // Solo enviamos si pasó el tiempo mínimo O si es la primera vez
                if (ahora - ultimoEnvio >= TIEMPO_MINIMO_ENVIO_MS) {
                    pasoElThrottling = true;
                    ultimaActualizacionWS.put(evento.getVehicleId().toString(), ahora);
                }
            }

            if (pasoElThrottling) {
                VehiculoRedisDto dto = telemetriaMapper.avroToDto(evento);
                dto.setStatus(estadoActual);
                paraEnviarWebsocket.add(dto);
                paraGuardarRedis.add(evento);
            }
        }

        // Bloque de escritura en Redis y Envío WS
        if (!paraGuardarRedis.isEmpty()) {
            log.info("💾 Guardando {} vehículos en Redis y notificando...", paraGuardarRedis.size());

            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (TelemetriaVehiculo ev : paraGuardarRedis) {
                    byte[] key = ("vehiculo:" + ev.getVehicleId()).getBytes(StandardCharsets.UTF_8);

                    Map<byte[], byte[]> hash = new HashMap<>();

                    if (ev.getLatitude() != null) hash.put("lat".getBytes(), String.valueOf(ev.getLatitude()).getBytes());
                    if (ev.getLongitude() != null) hash.put("lon".getBytes(), String.valueOf(ev.getLongitude()).getBytes());
                    if (ev.getSpeed() != null) hash.put("speed".getBytes(), String.valueOf(ev.getSpeed()).getBytes());
                    if (ev.getBatteryLevel() != null) hash.put("bat".getBytes(), String.valueOf(ev.getBatteryLevel()).getBytes());
                    if (ev.getTimestamp() != null) hash.put("ts".getBytes(), String.valueOf(ev.getTimestamp()).getBytes());

                    connection.hSetNX(key, "status".getBytes(), ESTADO_DEFECTO.getBytes());

                    if (!hash.isEmpty()) {
                        connection.hMSet(key, hash);
                        connection.expire(key, 3600);
                    }
                }
                return null;
            });

            // Llamada a WebSocket
            distribuirAWebSockets(paraEnviarWebsocket);
        } else {
            log.info("⏭️ Todos los eventos del lote fueron filtrados (sin movimiento o por tiempo).");
        }
    }

    private void distribuirAWebSockets(List<VehiculoRedisDto> dtos) {
        if (dtos.isEmpty()) return;

        log.info("📡 SOCKET: Enviando lote de {} vehículos.", dtos.size());

        // 1. Canal ADMIN: Recibe la lista completa
        messagingTemplate.convertAndSend("/topic/admin/updates", dtos);

        // 2. Canal PÚBLICO: Filtramos la lista y enviamos un sub-conjunto
        List<VehiculoRedisDto> lotePublico = dtos.stream()
                .filter(dto -> dto.getStatus() == EstadoCirculacion.ACTIVO ||
                        dto.getStatus() == EstadoCirculacion.CARGANDO ||
                        dto.getStatus() == EstadoCirculacion.RUTA_LIBRE)
                .toList();

        if (!lotePublico.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/public/updates", lotePublico);
        }
    }

    private String safeConvertToString(Object obj) {
        return switch (obj) {
            case null -> null;
            case String s -> s;
            case byte[] bytes -> new String(bytes, StandardCharsets.UTF_8);
            default -> obj.toString();
        };
    }


    public void actualizarEstadoManual(EstadoDto cambioDto) {
        String vehicleId = cambioDto.vehiculoId();
        String key = "vehiculo:" + vehicleId;

        // Corrección del ClassCastException usando .name()
        String nuevoEstadoStr = EstadoCirculacion.values()[cambioDto.nuevoEsatado()].name();

        redisTemplate.opsForHash().put(key, "status", nuevoEstadoStr);

        // Recuperar datos para notificar cambio inmediato (sin throttling)
        List<Object> rawDataList = redisTemplate.opsForHash().multiGet(key,
                Arrays.asList("lat", "lon", "speed", "bat", "ts", "status"));

        if (rawDataList != null && rawDataList.get(0) != null) {
            Map<Object, Object> simulatedMap = new HashMap<>();
            simulatedMap.put("lat", rawDataList.get(0));
            simulatedMap.put("lon", rawDataList.get(1));
            simulatedMap.put("speed", rawDataList.get(2));
            simulatedMap.put("bat", rawDataList.get(3));
            simulatedMap.put("ts", rawDataList.get(4));
            simulatedMap.put("status", rawDataList.get(5));

            VehiculoRedisDto dto = telemetriaMapper.mapFromRedis(vehicleId, simulatedMap);

            distribuirAWebSockets(Collections.singletonList(dto));
        }
    }
}
