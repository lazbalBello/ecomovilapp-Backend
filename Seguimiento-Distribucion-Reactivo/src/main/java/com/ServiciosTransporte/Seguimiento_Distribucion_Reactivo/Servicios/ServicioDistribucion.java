package com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Servicios;

import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.EstadoCirculacion;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.EstadoDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Dtos.VehiculoRedisDto;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Mappers.TelemetriaMapper;
import com.ServiciosTransporte.Seguimiento_Distribucion_Reactivo.Utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.servicioTransporte.flota.eventos.vehiculo.seguimiento.TelemetriaVehiculo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class ServicioDistribucion {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final KafkaReceiver<String, TelemetriaVehiculo> kafkaReceiver;
    private final TelemetriaMapper telemetriaMapper;
    private final ObjectMapper objectMapper;

    private final Sinks.Many<String> sinkAdmin = Sinks.many().replay().latest();
    private final Sinks.Many<String> sinkPublico = Sinks.many().replay().latest();

    private static final String ESTADO_DEFECTO = EstadoCirculacion.ACTIVO.name();
    private static final double UMBRAL_MOVIMIENTO_METROS = 10.0;
    private static final long TIEMPO_MINIMO_ENVIO_MS = 2000;
    private final Map<String, Long> ultimaActualizacionWS = new ConcurrentHashMap<>();

    @PostConstruct
    public void iniciarConsumo() {
        kafkaReceiver.receive()
                // 1. Agrupamiento (Batching) similar a tu configuración anterior
                .bufferTimeout(500, Duration.ofMillis(500))
                .flatMap(this::procesarLote)
                .subscribe(
                        null, // OnNext (ya manejado internamente)
                        error -> log.error("❌ Error crítico en flujo reactivo de telemetría", error)
                );
    }

    private Mono<Void> procesarLote(List<ReceiverRecord<String, TelemetriaVehiculo>> registros) {
        if (registros.isEmpty()) return Mono.empty();

        log.info("📥 Kafka Reactivo: Procesando lote de {} eventos", registros.size());

        // Deduplicación (misma lógica de negocio: quedarse con el último del lote)
        return Flux.fromIterable(registros)
                .groupBy(record -> record.value().getVehicleId())
                .flatMap(group -> group.reduce((a, b) -> b)) // Reducir al más reciente por ID
                .flatMap(record -> procesarVehiculoIndividual(record.value()))
                .then();
    }

    private Mono<Void> procesarVehiculoIndividual(TelemetriaVehiculo evento) {
        String key = "vehiculo:" + evento.getVehicleId();

        // Operación No Bloqueante a Redis
        return redisTemplate.opsForHash().multiGet(key, List.of("lat", "lon", "status"))
                .flatMap(redisData -> {
                    boolean actualizar = false;
                    boolean pasoElThrottling = false;

                    // Lógica de Negocio (Replicada Exactamente)
                    // Nota: redisData.get(0) es lat, get(1) es lon, get(2) es status
                    EstadoCirculacion estadoActual = parseEstado(redisData.size() > 2 ? redisData.get(2) : null);

                    // Validar movimiento
                    if (redisData.size() >= 2 && redisData.get(0) != null && redisData.get(1) != null) {
                        try {
                            double latAnt = Double.parseDouble(redisData.get(0).toString());
                            double lonAnt = Double.parseDouble(redisData.get(1).toString());
                            double distancia = GeoUtils.calcularDistanciaMetros(latAnt, lonAnt, evento.getLatitude(), evento.getLongitude());

                            if (distancia >= UMBRAL_MOVIMIENTO_METROS) {
                                actualizar = true;
                            }
                        } catch (NumberFormatException e) {
                            actualizar = true; // Ante error de datos, actualizar
                        }
                    } else {
                        actualizar = true; // Primera vez
                    }

                    // Throttling en memoria (ConcurrentHashMap es seguro y rápido aquí)
                    if (actualizar) {
                        long ahora = System.currentTimeMillis();
                        long ultimoEnvio = ultimaActualizacionWS.getOrDefault(evento.getVehicleId().toString(), 0L);
                        if (ahora - ultimoEnvio >= TIEMPO_MINIMO_ENVIO_MS) {
                            pasoElThrottling = true;
                            ultimaActualizacionWS.put(evento.getVehicleId().toString(), ahora);
                        }
                    }

                    if (pasoElThrottling) {
                        return guardarYNotificar(evento, estadoActual);
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> guardarYNotificar(TelemetriaVehiculo evento, EstadoCirculacion estadoActual) {
        String key = "vehiculo:" + evento.getVehicleId();

        // Mapeo manual para Redis (como en tu Mapper)
        Map<String, String> data = new HashMap<>();
        data.put("lat", String.valueOf(evento.getLatitude()));
        data.put("lon", String.valueOf(evento.getLongitude()));
        data.put("speed", String.valueOf(evento.getSpeed()));
        data.put("bat", String.valueOf(evento.getBatteryLevel()));
        data.put("ts", String.valueOf(evento.getTimestamp()));
        // Importante: status se setea solo si es nuevo, pero usamos putAll que sobrescribe.
        // Mantenemos lógica de setNX para status si quisiéramos, pero aquí asumimos update.
        // Si quieres mantener status antiguo, no lo envíes en el putAll.

        return redisTemplate.opsForHash().putAll(key, data)
                .then(redisTemplate.opsForHash().putIfAbsent(key, "status", ESTADO_DEFECTO)) // Solo si no existe
                .then(redisTemplate.expire(key, Duration.ofHours(1)))
                .then(Mono.defer(() -> {
                    // Construir DTO y Emitir
                    VehiculoRedisDto dto = telemetriaMapper.avroToDto(evento);
                    dto.setStatus(estadoActual); // Usamos el estado que leímos de Redis (o default)

                    return emitirAWebSockets(dto);
                }));
    }

    private Mono<Void> emitirAWebSockets(VehiculoRedisDto dto) {
        try {
            String json = objectMapper.writeValueAsString(dto);

            // 1. Emitir a Admin (Todo)
            sinkAdmin.tryEmitNext(json);

            // 2. Emitir a Público (Filtrado)
            if (esVisibleParaPublico(dto.getStatus())) {
                sinkPublico.tryEmitNext(json);
            }
        } catch (JsonProcessingException e) {
            log.error("Error serializando JSON", e);
        }
        return Mono.empty();
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

    // --- Métodos Públicos para Exponer los Flujos ---
    public Flux<String> getFlujoAdmin() {
        return sinkAdmin.asFlux();
    }

    public Flux<String> getFlujoPublico() {
        return sinkPublico.asFlux();
    }

    // --- Actualización Manual (Reactiva) ---
    public Mono<Void> actualizarEstadoManual(EstadoDto cambioDto) {
        String key = "vehiculo:" + cambioDto.vehiculoId();
        String nuevoEstadoStr = EstadoCirculacion.values()[cambioDto.nuevoEsatado()].name();

        return redisTemplate.opsForHash().put(key, "status", nuevoEstadoStr)
                .then(redisTemplate.opsForHash().entries(key).collectMap(Map.Entry::getKey, Map.Entry::getValue))
                .flatMap(mapa -> {
                    if (mapa.isEmpty()) return Mono.empty();

                    // Adaptar Map<Object, Object> a Map<Object, Object> para el mapper existente
                    // (En Redis Reactive las keys son Strings, el mapper espera Objects, compatible)
                    Map<Object, Object> mapaObj = new HashMap<>(mapa);
                    VehiculoRedisDto dto = telemetriaMapper.mapFromRedis(cambioDto.vehiculoId(), mapaObj);

                    return emitirAWebSockets(dto);
                });
    }
}
