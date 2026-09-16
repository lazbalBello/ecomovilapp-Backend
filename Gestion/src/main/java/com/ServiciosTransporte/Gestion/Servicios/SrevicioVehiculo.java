package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Dto.VehiculoDto;
import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.VehiculoUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.VehiculoLiteDtoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
import com.ServiciosTransporte.Gestion.MappersUpdate.VehiculoUpdateMapper;
import com.servicioTransporte.flota.eventos.vehiculo.configuracion.DispositivoGpsAutorizado;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SrevicioVehiculo {

    private static final String TOPIC_DISPOSITIVOS_AUTORIZADOS = "flota-dispositivos-autorizados";

    private final IRepositorioVehiculo repositorioVehiculo;
    private final VehiculoMapper vehiculoMapper;
    private final VehiculoLiteDtoMapper vehiculoLiteDtoMapper;
    private final VehiculoUpdateMapper vehiculoUpdateMapper;
    private final IRepositorioRuta repositorioRuta;
    private final IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public VehiculoDto registrarVehiculo(VehiculoDto vehiculoDto){
        Vehiculo vehiculo = vehiculoMapper.toVehiculo(vehiculoDto);
        Vehiculo vehiculoGuardado = repositorioVehiculo.save(vehiculo);

        if (vehiculoGuardado.getGpsId() != null && !vehiculoGuardado.getGpsId().trim().isEmpty()) {
            publicarEventoGps(vehiculoGuardado.getGpsId(), vehiculoGuardado.getMatricula(), true);
        }

        return vehiculoMapper.toVehiculoDto(vehiculoGuardado);
    }

    public List<VehiculoLiteDto> listarVehiculos(){
         List<Vehiculo> vehiculos = repositorioVehiculo.findAll();
         return vehiculos.stream()
                 .map(vehiculoLiteDtoMapper::toVehiculoLiteDto)
                 .collect(Collectors.toList());
    }

    public VehiculoLiteDto buscarPorId(Long Id){
        Vehiculo vehiculo = repositorioVehiculo.findById(Id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el vehículo con el Id" + Id));
        return vehiculoLiteDtoMapper.toVehiculoLiteDto(vehiculo);
    }

    public VehiculoLiteDto buscarPorMatricula(String matricula){
        Vehiculo vehiculo = repositorioVehiculo.findByMatricula(matricula)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el vehículo con la matricula" + matricula));

        return vehiculoLiteDtoMapper.toVehiculoLiteDto(vehiculo);
    }

    public VehiculoLiteDto buscarPorGpsId(String gpsId){
        Vehiculo vehiculo = repositorioVehiculo.findByGpsId(gpsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el vehículo con el GPS ID: " + gpsId));

        return vehiculoLiteDtoMapper.toVehiculoLiteDto(vehiculo);
    }

    public List<VehiculoLiteDto> filtrarPorMatricula(String matricula){
        List<Vehiculo> vehiculos = repositorioVehiculo.findByMatriculaContainingIgnoreCase(matricula);
        return vehiculos.stream()
                .map(vehiculoLiteDtoMapper::toVehiculoLiteDto)
                .collect(Collectors.toList());
    }

    public List<String> sugerirMaricula(String matricula){
        List<Vehiculo> vehiculos = repositorioVehiculo.findByMatriculaContainingIgnoreCase(matricula);
        return vehiculos.stream()
                .map(Vehiculo::getMatricula)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehiculoLiteDto actualizarVehiculo(Long id, VehiculoUpdateDto updateDto){
        Vehiculo vehiculo = repositorioVehiculo.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró el vehiculo con el id " + id));

        String gpsAnterior = vehiculo.getGpsId();

        if (updateDto.getRutaId() != null){
            Ruta nuevaRuta = repositorioRuta.findById(updateDto.getRutaId())
                    .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
            vehiculo.setRuta(nuevaRuta);
        }
        vehiculoUpdateMapper.updateVehiculoFromDto(updateDto, vehiculo);
        Vehiculo actualizado = repositorioVehiculo.save(vehiculo);

        String gpsNuevo = actualizado.getGpsId();
        // Si cambió el GPS asociado al vehículo:
        if (gpsAnterior != null && !gpsAnterior.trim().isEmpty() && !gpsAnterior.equals(gpsNuevo)) {
            publicarEventoGps(gpsAnterior, actualizado.getMatricula(), false);
        }
        if (gpsNuevo != null && !gpsNuevo.trim().isEmpty() && !gpsNuevo.equals(gpsAnterior)) {
            publicarEventoGps(gpsNuevo, actualizado.getMatricula(), true);
        }

        return vehiculoLiteDtoMapper.toVehiculoLiteDto(actualizado);
    }

    @Transactional
    public VehiculoLiteDto desasociarGps(Long id){
        Vehiculo vehiculo = repositorioVehiculo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el vehículo con el id " + id));

        String gpsAnterior = vehiculo.getGpsId();
        if (gpsAnterior == null || gpsAnterior.trim().isEmpty()) {
            return vehiculoLiteDtoMapper.toVehiculoLiteDto(vehiculo);
        }

        vehiculo.setGpsId(null);
        Vehiculo actualizado = repositorioVehiculo.save(vehiculo);

        publicarEventoGps(gpsAnterior, actualizado.getMatricula(), false);

        return vehiculoLiteDtoMapper.toVehiculoLiteDto(actualizado);
    }

    /** Devuelve todos los vehículos que han sido eliminados (soft delete). */
    @Transactional
    public List<VehiculoLiteDto> listarEliminados(){
        return repositorioVehiculo.findAllEliminados().stream()
                .map(vehiculoLiteDtoMapper::toVehiculoLiteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void softDeleteVehiculo(Long id){
        Vehiculo vehiculo = repositorioVehiculo.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Vehiculo no encontrado con Id " + id));

        String gpsId = vehiculo.getGpsId();
        vehiculo.setFechaEliminacion(LocalDateTime.now());
        vehiculo.setGpsId(null); // Liberar el GPS para poder reasignarlo a otro vehículo
        repositorioVehiculo.save(vehiculo);

        repositorioVehiculoAsignacion.softDeleteFromVehiculo(id, LocalDateTime.now());

        if (gpsId != null && !gpsId.trim().isEmpty()) {
            publicarEventoGps(gpsId, vehiculo.getMatricula(), false);
        }
    }

    /**
     * Sincronización idempotente en el arranque: garantiza que el topic compactado de Kafka
     * contenga todos los GPS activos registrados en la base de datos.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void sincronizarGpsAlIniciar() {
        try {
            // @SQLRestriction filtra automáticamente los eliminados; solo buscamos los que tienen GPS
            List<Vehiculo> vehiculosActivos = repositorioVehiculo.findByGpsIdIsNotNull();
            log.info("Iniciando sincronización de {} GPS activos con el topic Kafka...", vehiculosActivos.size());
            for (Vehiculo v : vehiculosActivos) {
                if (v.getGpsId() != null && !v.getGpsId().trim().isEmpty()) {
                    publicarEventoGps(v.getGpsId(), v.getMatricula(), true);
                }
            }
            log.info("Sincronización inicial de GPS con Kafka completada.");
        } catch (Exception e) {
            log.error("Error al sincronizar dispositivos GPS iniciales en Kafka: {}", e.getMessage(), e);
        }
    }

    /**
     * Publica un evento Avro en el topic compactado usando gpsId como clave.
     */
    private void publicarEventoGps(String gpsId, String matricula, boolean activo) {
        if (gpsId == null || gpsId.trim().isEmpty()) {
            return;
        }

        String idLimpio = gpsId.trim();
        DispositivoGpsAutorizado evento = DispositivoGpsAutorizado.newBuilder()
                .setGpsId(idLimpio)
                .setMatricula(matricula)
                .setActivo(activo)
                .setTimestamp(Instant.now())
                .build();

        // Enviar con gpsId como key para que la compactación de Kafka funcione por dispositivo
        kafkaTemplate.send(TOPIC_DISPOSITIVOS_AUTORIZADOS, idLimpio, evento)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Fallo al publicar evento de autorización GPS para {}: {}", idLimpio, ex.getMessage(), ex);
                    } else {
                        log.info("Evento GPS emitido a Kafka topic {}: gpsId={}, activo={}",
                                TOPIC_DISPOSITIVOS_AUTORIZADOS, idLimpio, activo);
                    }
                });
    }
}
