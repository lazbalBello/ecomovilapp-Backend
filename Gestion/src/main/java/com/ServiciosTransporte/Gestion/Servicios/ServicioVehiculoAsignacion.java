package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Dto.VehiculoAsignacionDto;
import com.ServiciosTransporte.Gestion.DtoResponse.AsignacionLiteDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.AsignacionUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoAsignacionMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.AsignacionLiteDtoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioConductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
import com.ServiciosTransporte.Gestion.MappersUpdate.AsignacionUpdateDtoMapper;
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
    private final AsignacionLiteDtoMapper asignacionLiteDtoMapper;
    private final AsignacionUpdateDtoMapper asignacionUpdateDtoMapper;

    @Transactional
    public AsignacionLiteDto asignarConductorAVehiculo(VehiculoAsignacionDto vadto){
        Vehiculo vehiculo = repositorioVehiculo.findById(vadto.getVehiculoId())
                .orElseThrow(()-> new RuntimeException("Vehiculo no encontrado"));
        Conductor conductorAsignado = repositorioConductor.findById(vadto.getConductorId())
                .orElseThrow(()-> new RuntimeException("Conductor no encontrado"));
        repositorioVehiculoAsignacion.findByVehiculo_IdAndConductor_Id(vadto.getVehiculoId(),vadto.getConductorId())
                .ifPresent(existing ->{
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe una asignación con el conductor y vehiculo seleccionado");
                });
        VehiculoAsignacion asignacion = vehiculoAsignacionMapper.toVehiculoAsignacion(vadto);
        asignacion.setVehiculo(vehiculo);
        asignacion.setConductor(conductorAsignado);
        VehiculoAsignacion asignacionRegistrada = repositorioVehiculoAsignacion.save(asignacion);
        return asignacionLiteDtoMapper.toAsignacionLiteDto(asignacionRegistrada);
    }

    public List<AsignacionLiteDto> listarTodo(){
        List<VehiculoAsignacion> vehiculos = repositorioVehiculoAsignacion.findAll();
        return vehiculos.stream()
                .map(asignacionLiteDtoMapper::toAsignacionLiteDto)
                .collect(Collectors.toList());
    }

    public AsignacionLiteDto buscarPorId(Long id){
        VehiculoAsignacion asignacion = repositorioVehiculoAsignacion.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la asignacion con el id " + id));
        return asignacionLiteDtoMapper.toAsignacionLiteDto(asignacion);
    }

    @Transactional
    public AsignacionLiteDto actualizarAsignacion(Long id, AsignacionUpdateDto updateDto){
        VehiculoAsignacion asignacion = repositorioVehiculoAsignacion.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Asignación no encontrada"));
        asignacionUpdateDtoMapper.updateAsignacionFromDto(updateDto, asignacion);
        VehiculoAsignacion actualizada = repositorioVehiculoAsignacion.save(asignacion);
        return asignacionLiteDtoMapper.toAsignacionLiteDto(actualizada);
    }

    @Transactional
    public void softDeleteAsignacion(Long id){
        VehiculoAsignacion asignacion = repositorioVehiculoAsignacion.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Asignación no encontrada con el id " + id));

        asignacion.setFechaEliminacion(LocalDateTime.now());
        repositorioVehiculoAsignacion.save(asignacion);
    }
}
