package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Auditoria.Anotaciones.Auditable;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Dto.ConductorDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Conductores.ConductorLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Conductores.ConductorSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.ConductorUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.ConductorMapper;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioConductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculoAsignacion;
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
    private final IRepositorioVehiculoAsignacion repositorioVehiculoAsignacion;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    @Auditable(tabla = "conductor", operacion = TipoOperacion.CREACION, entidad = Conductor.class)
    public ConductorDto registrarConductor(@Valid ConductorDto conductorDto){
        Conductor conductor = conductorMapper.toConductor(conductorDto);
        Conductor conductorGuardado = repositorioConductor.save(conductor);
        return conductorMapper.toConductorDto(conductorGuardado);
    }

    public ConductorLiteDto buscarPorDni(String dni){
        Conductor conductor = repositorioConductor.findByDni(dni)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró el conductor con el dni" + dni));

        return conductorMapper.toConductorLiteDto(conductor);
    }

    public List<ConductorLiteDto> filtrarPorDni(String dni){
        List<Conductor> conductores = repositorioConductor.findByDniContainingIgnoreCase(dni);
        return conductores.stream()
                .map(conductorMapper::toConductorLiteDto)
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
                 .map(conductorMapper::toConductorSugerenciaDto)
                 .collect(Collectors.toList());
    }

    public ConductorLiteDto buscarPorId(Long Id){
        Conductor conductor = repositorioConductor.findById(Id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se enctró el conductor con el id " + Id));
        return conductorMapper.toConductorLiteDto(conductor);
    }

    public List<ConductorLiteDto> listarTodo(){
        List<Conductor> conductores = repositorioConductor.findAll();
        return conductores.stream()
                .map(conductorMapper::toConductorLiteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(tabla = "conductor", operacion = TipoOperacion.MODIFICACION, entidad = Conductor.class)
    public ConductorLiteDto actualizarConductor(Long id, ConductorUpdateDto updateDto){
        Conductor conductor = repositorioConductor.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró el conductor con el id " + id));

        if (updateDto.dni() != null && !updateDto.dni().trim().isEmpty()
                && !updateDto.dni().trim().equalsIgnoreCase(conductor.getDni())) {
            repositorioConductor.findByDni(updateDto.dni().trim())
                    .ifPresent(otro -> {
                        if (!otro.getId().equals(id)) {
                            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                    "Ya existe un conductor con el DNI: " + updateDto.dni().trim());
                        }
                    });
        }

        conductorMapper.updateConductorFromDto(updateDto,conductor);
        if (updateDto.categoriasLicencia() != null)
            conductor.getCategoriasLicencia().addAll(updateDto.categoriasLicencia());
        Conductor actualizado = repositorioConductor.save(conductor);
        return conductorMapper.toConductorLiteDto(actualizado);
    }

    /** Devuelve todos los conductores que han sido eliminados (soft delete). */
    @Transactional
    public List<ConductorLiteDto> listarEliminados(){
        return repositorioConductor.findAllEliminados().stream()
                .map(conductorMapper::toConductorLiteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(tabla = "conductor", operacion = TipoOperacion.SOFT_DELETE, entidad = Conductor.class)
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
