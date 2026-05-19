package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Dto.ConductorDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ConductorLiteDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ConductorSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.ConductorUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.ConductorMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.ConductorLiteDtoMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.ConductorSugerenciaDtoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioConductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
import com.ServiciosTransporte.Gestion.MappersUpdate.ConductorUpdateDtoMapper;
import com.servicioTransporte.flota.eventos.conductor.eliminar.ConductorEliminado;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioConductor {

    private static final String TOPIC_ELIMINAR_CONDUCTOR = "eliminar-conductor";

    private final IRepositorioConductor repositorioConductor;
    private final ConductorMapper conductorMapper;
    private final ConductorLiteDtoMapper conductorLiteDtoMapper;
    private final ConductorSugerenciaDtoMapper conductorSugerenciaDtoMapper;
    private final ConductorUpdateDtoMapper conductorUpdateDtoMapper;
    private final IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public ConductorDto registrarConductor(@Valid ConductorDto conductorDto){
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

        if (conductor.getUsuarioId() != null) {
            ConductorEliminado evento = ConductorEliminado.newBuilder()
                    .setKeycloakId(conductor.getUsuarioId())
                    .setNombre(conductor.getNombre())
                    .setApellido(conductor.getApellidos())
                    .setDni(conductor.getDni())
                    .build();
            kafkaTemplate.send(TOPIC_ELIMINAR_CONDUCTOR, conductor.getUsuarioId(), evento);
        }
    }
}
