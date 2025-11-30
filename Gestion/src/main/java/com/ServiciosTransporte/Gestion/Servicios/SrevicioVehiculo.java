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
public class SrevicioVehiculo {

    private final IRepositorioVehiculo repositorioVehiculo;
    private final VehiculoMapper vehiculoMapper;
    private final VehiculoLiteDtoMapper vehiculoLiteDtoMapper;
    private final VehiculoUpdateMapper vehiculoUpdateMapper;
    private final IRepositorioRuta repositorioRuta;
    private final IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;

    @Transactional
    public VehiculoDto registrarVehiculo(VehiculoDto vehiculoDto){
        Vehiculo vehiculo = vehiculoMapper.toVehiculo(vehiculoDto);
        Vehiculo vehiculoGuardado = repositorioVehiculo.save(vehiculo);
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
        if (updateDto.getRutaId() != null){
            Ruta nuevaRuta = repositorioRuta.findById(updateDto.getRutaId())
                    .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
            vehiculo.setRuta(nuevaRuta);
        }
        vehiculoUpdateMapper.updateVehiculoFromDto(updateDto, vehiculo);
        Vehiculo actualizado = repositorioVehiculo.save(vehiculo);
        return vehiculoLiteDtoMapper.toVehiculoLiteDto(actualizado);
    }

    @Transactional
    public void softDeleteVehiculo(Long id){
        Vehiculo vehiculo = repositorioVehiculo.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Vehiculo no encontrado con Id " + id));

        vehiculo.setFechaEliminacion(LocalDateTime.now());
        repositorioVehiculo.save(vehiculo);

        repositorioVehiculoAsignacion.softDeleteFromVehiculo(id, LocalDateTime.now());
    }
}
