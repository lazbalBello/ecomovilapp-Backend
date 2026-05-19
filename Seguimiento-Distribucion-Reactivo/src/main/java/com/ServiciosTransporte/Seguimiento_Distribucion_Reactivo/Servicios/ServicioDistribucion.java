package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Servicios;

import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.EstadoCirculacion;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.EstadoDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.TelemetriaJsonDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.VehiculoRedisDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Mappers.TelemetriaMapper;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Mqtt.Config.MqttPublisherConfig.MqttPublisher;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioDistribucion {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final TelemetriaMapper telemetriaMapper;
    private final ObjectMapper objectMapper;
    private final MqttPublisher mqttPublisher;

    @Value("${spring.mqtt.topics.admin:vehiculos/%s/telemetria/admin}")
    private String topicAdminFormat;

    @Value("${spring.mqtt.topics.publico:vehiculos/%s/telemetria/publico}")
    private String topicPublicoFormat;

    private static final String ESTADO_DEFECTO = EstadoCirculacion.ACTIVO.name();
    private static final double UMBRAL_MOVIMIENTO_METROS = 10.0;
    private static final long TIEMPO_MINIMO_ENVIO_MS = 2000;

    // Cambiado a String para facilitar el manejo con los datos de Avro
    private final Map<String, Long> ultimaActualizacionWS = new ConcurrentHashMap<>();

    /**
     * Nuevo punto de entrada que procesa directamente el evento Avro desde Kafka.
     */
    public Mono<Void> procesarTelemetriaKafka(TelemetriaVehiculo evento) {
        // 1. Validación de vehicleId (Resiliencia ante campos vacíos)
        if (evento.getVehicleId() == null || evento.getVehicleId().toString().trim().isEmpty()) {
            log.warn("VehicleId vacío recibido desde Kafka. Se descarta el evento.");
            return Mono.empty();
        }

        String vehiculoId = evento.getVehicleId().toString();

        // 2. Validación de rangos geográficos (Evitar saltos del GPS a coordenadas 0,0)
        double lat = evento.getLatitude();
        double lon = evento.getLongitude();

        if (lat < -90 || lat > 90 || lon < -180 || lon > 180 || (lat == 0.0 && lon == 0.0)) {
            log.warn("Telemetría corrupta o fuera de rango descartada para vehículo {}: lat={}, lon={}",
                    vehiculoId, lat, lon);
            return Mono.empty();
        }

        // 3. Flujo principal: Evaluar movimiento, guardar en Redis y publicar en EMQX
        return procesarVehiculoIndividual(evento, vehiculoId);
    }

    private Mono<Void> procesarVehiculoIndividual(TelemetriaVehiculo evento, String vehiculoId) {
        String key = "vehiculo:" + vehiculoId;

        return redisTemplate.opsForHash().multiGet(key, List.of("lat", "lon", "status"))
                .flatMap(redisData -> {
                    boolean actualizar = false;
                    boolean pasoElThrottling = false;

                    EstadoCirculacion estadoActual = parseEstado(redisData.size() > 2 ? redisData.get(2) : null);

                    // Validar si el vehículo se ha movido más del umbral permitido
                    if (redisData.size() >= 2 && redisData.get(0) != null && redisData.get(1) != null) {
                        try {
                            double latAnt = Double.parseDouble(redisData.get(0).toString());
                            double lonAnt = Double.parseDouble(redisData.get(1).toString());
                            double distancia = GeoUtils.calcularDistanciaMetros(latAnt, lonAnt,
                                    evento.getLatitude(), evento.getLongitude());

                            if (distancia >= UMBRAL_MOVIMIENTO_METROS) {
                                actualizar = true;
                            }
                        } catch (NumberFormatException e) {
                            actualizar = true; // Ante datos corruptos previos en Redis, forzar actualización
                        }
                    } else {
                        actualizar = true; // Primera lectura del vehículo
                    }

                    // Throttling: Proteger a los clientes y a EMQX de ráfagas de mensajes
                    if (actualizar) {
                        long ahora = System.currentTimeMillis();
                        long ultimoEnvio = ultimaActualizacionWS.getOrDefault(vehiculoId, 0L);
                        if (ahora - ultimoEnvio >= TIEMPO_MINIMO_ENVIO_MS) {
                            pasoElThrottling = true;
                            ultimaActualizacionWS.put(vehiculoId, ahora);
                        }
                    }

                    if (pasoElThrottling) {
                        return guardarYNotificar(evento, vehiculoId, estadoActual);
                    }
                    return Mono.empty();
                })
                .onErrorResume(e -> {
                    log.error("Error al procesar vehículo {} contra Redis: {}", vehiculoId, e.getMessage());
                    return Mono.empty();
                });
    }

    private Mono<Void> guardarYNotificar(TelemetriaVehiculo evento, String vehiculoId, EstadoCirculacion estadoActual) {
        String key = "vehiculo:" + vehiculoId;

        Map<String, String> data = new HashMap<>();
        data.put("lat", String.valueOf(evento.getLatitude()));
        data.put("lon", String.valueOf(evento.getLongitude()));
        data.put("speed", String.valueOf(evento.getSpeed()));
        data.put("bat", String.valueOf(evento.getBatteryLevel()));
        data.put("ts", String.valueOf(evento.getTimestamp()));

        return redisTemplate.opsForHash().putAll(key, data)
                .then(redisTemplate.opsForHash().putIfAbsent(key, "status", ESTADO_DEFECTO))
                .then(redisTemplate.expire(key, Duration.ofHours(1)))
                .then(Mono.fromCallable(() -> {
                    VehiculoRedisDto dto = telemetriaMapper.avroToDto(evento);
                    dto.setStatus(estadoActual);
                    return dto;
                }))
                .flatMap(this::emitirAMqtt);
    }

    private Mono<Void> emitirAMqtt(VehiculoRedisDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);
            String vehiculoId = dto.getVehicleId();

            String topicAdmin   = String.format(topicAdminFormat, vehiculoId);
            String topicPublico = String.format(topicPublicoFormat, vehiculoId);

            Mono<Void> emitAdmin   = mqttPublisher.publicar(topicAdmin, json);
            Mono<Void> emitPublico = esVisibleParaPublico(dto.getStatus())
                    ? mqttPublisher.publicar(topicPublico, json)
                    : Mono.empty();

            // Ejecuta ambas publicaciones en paralelo sin bloquear
            return Mono.whenDelayError(emitAdmin, emitPublico);

        } catch (JsonProcessingException e) {
            log.error("Error serializando JSON para MQTT del vehículo {}", dto.getVehicleId(), e);
            return Mono.empty();
        }
    }

    private boolean esVisibleParaPublico(EstadoCirculacion estado) {
        return estado == EstadoCirculacion.ACTIVO ||
                estado == EstadoCirculacion.CARGANDO ||
                estado == EstadoCirculacion.RUTA_LIBRE;
    }

    private EstadoCirculacion parseEstado(Object val) {
        if (val == null) return EstadoCirculacion.ACTIVO;
        try {
            return EstadoCirculacion.valueOf(val.toString());
        } catch (Exception e) {
            return EstadoCirculacion.ACTIVO;
        }
    }

    public Mono<Void> actualizarEstadoManual(EstadoDto cambioDto) {
        String key = "vehiculo:" + cambioDto.vehiculoId();
        String nuevoEstadoStr = EstadoCirculacion.values()[cambioDto.nuevoEsatado()].name();

        return redisTemplate.opsForHash().put(key, "status", nuevoEstadoStr)
                .then(redisTemplate.opsForHash().entries(key).collectMap(Map.Entry::getKey, Map.Entry::getValue))
                .flatMap(mapa -> {
                    if (mapa.isEmpty()) return Mono.empty();
                    Map<Object, Object> mapaObj = new HashMap<>(mapa);
                    VehiculoRedisDto dto = telemetriaMapper.mapFromRedis(cambioDto.vehiculoId(), mapaObj);
                    return emitirAMqtt(dto);
                });
    }
}
