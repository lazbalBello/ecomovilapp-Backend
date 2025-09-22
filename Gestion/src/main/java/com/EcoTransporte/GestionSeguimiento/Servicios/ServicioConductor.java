package com.EcoTransporte.GestionSeguimiento.Servicios;

import com.EcoTransporte.GestionSeguimiento.Dto.ConductorDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.ConductorLiteDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.ConductorSugerenciaDto;
import com.EcoTransporte.GestionSeguimiento.DtoUpdate.ConductorUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Mappers.ConductorMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.ConductorLiteDtoMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.ConductorSugerenciaDtoMapper;
import com.EcoTransporte.GestionSeguimiento.Modelos.Conductor;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioConductor;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioVehiculoAsignacion;
import com.EcoTransporte.GestionSeguimiento.UpdateMappers.ConductorUpdateDtoMapper;
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
public class ServicioConductor {

    private final IRepositorioConductor repositorioConductor;
    private final ConductorMapper conductorMapper;
    private final ConductorLiteDtoMapper conductorLiteDtoMapper;
    private final ConductorSugerenciaDtoMapper conductorSugerenciaDtoMapper;
    private final ConductorUpdateDtoMapper conductorUpdateDtoMapper;
    private final IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;

    @Transactional
    public ConductorDto registrarConductor(ConductorDto conductorDto){
        Conductor conductor = conductorMapper.toConductor(conductorDto);
        Conductor conductorGuardado = repositorioConductor.save(conductor);
        return conductorMapper.toConductorDto(conductorGuardado);
    }

    public ConductorLiteDto buscarPorDni(String dni){
        Conductor conductor = repositorioConductor.findByDni(dni)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el conductor con el dni" + dni));

        return conductorLiteDtoMapper.toConductorLiteDto(conductor);
    }

    public List<ConductorLiteDto> filtrarPorDni(String dni){
        List<Conductor> conductores = repositorioConductor.findByDniContainingIgnoreCase(dni);
        return conductores.stream()
                .map(conductorLiteDtoMapper::toConductorLiteDto)
                .collect(Collectors.toList());
    }

    public List<String> sugerirDni(String dni){
        List<Conductor> conductores = repositorioConductor.findByDniContainingIgnoreCase(dni);
        return conductores.stream()
                .map(Conductor::getDni)
                .collect(Collectors.toList());
    }

    public List<ConductorSugerenciaDto> sugerirNombreYApellidos(String query){
         List<Conductor> conductores = repositorioConductor.findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(query, query);
         return conductores.stream()
                 .map(conductorSugerenciaDtoMapper::toConductorSugerenciaDto)
                 .collect(Collectors.toList());
    }

    public ConductorLiteDto buscarPorId(Long Id){
        Conductor conductor = repositorioConductor.findById(Id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se enctró el conductor con el id " + Id));
        return conductorLiteDtoMapper.toConductorLiteDto(conductor);
    }

    public List<ConductorLiteDto> listarTodo(){
        List<Conductor> conductores = repositorioConductor.findAll();
        return conductores.stream()
                .map(conductorLiteDtoMapper::toConductorLiteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ConductorLiteDto actualizarConductor(Long id, ConductorUpdateDto updateDto){
        Conductor conductor = repositorioConductor.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró el conductor con el id " + id));
        conductorUpdateDtoMapper.updateConductorFromDto(updateDto,conductor);
        if (updateDto.getCategoriasLicencia() != null)
            conductor.getCategoriasLicencia().addAll(updateDto.getCategoriasLicencia());
        Conductor actualizado = repositorioConductor.save(conductor);
        return conductorLiteDtoMapper.toConductorLiteDto(actualizado);
    }

    @Transactional
    public void softDeleteConductor(Long id){
        Conductor conductor = repositorioConductor.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Conductor no encontrado con id " + id));

        conductor.setFechaEliminacion(LocalDateTime.now());
        repositorioConductor.save(conductor);

        repositorioVehiculoAsignacion.softDeleteFromConductor(id, LocalDateTime.now());
    }
}
