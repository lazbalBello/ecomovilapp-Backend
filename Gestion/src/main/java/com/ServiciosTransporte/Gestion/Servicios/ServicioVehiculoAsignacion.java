package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Auditoria.Anotaciones.Auditable;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Dto.VehiculoAsignacionDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Asignaciones.AsignacionLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.AsignacionUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoAsignacionMapper;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioConductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioVehiculoAsignacion {

    private final IRepositorioVehiculo repositorioVehiculo;
    private final IRepositorioConductor repositorioConductor;
    private final VehiculoAsignacionMapper vehiculoAsignacionMapper;
    private final IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;
    private final VehiculoMapper vehiculoMapper;

    @Transactional
    @Auditable(tabla = "vehiculo_asignacion", operacion = TipoOperacion.CREACION, entidad = VehiculoAsignacion.class)
    public AsignacionLiteDto asignarConductorAVehiculo(VehiculoAsignacionDto vadto){
        Vehiculo vehiculo = repositorioVehiculo.findById(vadto.vehiculoId())
                .orElseThrow(()-> new RuntimeException("Vehiculo no encontrado"));
        Conductor conductorAsignado = repositorioConductor.findById(vadto.conductorId())
                .orElseThrow(()-> new RuntimeException("Conductor no encontrado"));
        repositorioVehiculoAsignacion.findByVehiculo_IdAndConductor_Id(vadto.vehiculoId(),vadto.conductorId())
                .ifPresent(existing ->{
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe una asignación con el conductor y vehiculo seleccionado");
                });
        VehiculoAsignacion asignacion = vehiculoAsignacionMapper.toVehiculoAsignacion(vadto);
        asignacion.setVehiculo(vehiculo);
        asignacion.setConductor(conductorAsignado);
        VehiculoAsignacion asignacionRegistrada = repositorioVehiculoAsignacion.save(asignacion);
        return vehiculoAsignacionMapper.toAsignacionLiteDto(asignacionRegistrada);
    }

    public List<AsignacionLiteDto> listarTodo(){
        List<VehiculoAsignacion> vehiculos = repositorioVehiculoAsignacion.findAll();
        return vehiculos.stream()
                .map(vehiculoAsignacionMapper::toAsignacionLiteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<VehiculoLiteDto> listarVehiculosActivosDelConductor(String usuarioId) {
        return repositorioVehiculoAsignacion.findVehiculosActivosByConductorUsuarioId(usuarioId).stream()
                .map(vehiculoMapper::toVehiculoLiteDto)
                .toList();
    }

    public AsignacionLiteDto buscarPorId(Long id){
        VehiculoAsignacion asignacion = repositorioVehiculoAsignacion.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la asignacion con el id " + id));
        return vehiculoAsignacionMapper.toAsignacionLiteDto(asignacion);
    }

    @Transactional
    @Auditable(tabla = "vehiculo_asignacion", operacion = TipoOperacion.MODIFICACION, entidad = VehiculoAsignacion.class)
    public AsignacionLiteDto actualizarAsignacion(Long id, AsignacionUpdateDto updateDto){
        VehiculoAsignacion asignacion = repositorioVehiculoAsignacion.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Asignación no encontrada"));
        vehiculoAsignacionMapper.updateAsignacionFromDto(updateDto, asignacion);
        VehiculoAsignacion actualizada = repositorioVehiculoAsignacion.save(asignacion);
        return vehiculoAsignacionMapper.toAsignacionLiteDto(actualizada);
    }

    /** Devuelve todas las asignaciones que han sido eliminadas (soft delete). */
    @Transactional
    public List<AsignacionLiteDto> listarEliminadas(){
        return repositorioVehiculoAsignacion.findAllEliminadas().stream()
                .map(vehiculoAsignacionMapper::toAsignacionLiteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(tabla = "vehiculo_asignacion", operacion = TipoOperacion.SOFT_DELETE, entidad = VehiculoAsignacion.class)
    public void softDeleteAsignacion(Long id){
        VehiculoAsignacion asignacion = repositorioVehiculoAsignacion.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Asignación no encontrada con el id " + id));

        asignacion.setFechaEliminacion(LocalDateTime.now());
        repositorioVehiculoAsignacion.save(asignacion);
    }
}
