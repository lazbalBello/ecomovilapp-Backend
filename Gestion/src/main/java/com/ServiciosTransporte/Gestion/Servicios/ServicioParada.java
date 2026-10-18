package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Auditoria.Anotaciones.Auditable;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Dto.ParadaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.ParadaUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.ParadaMapper;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioParada;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioParada {

    private final IRepositorioParada repositorioParada;
    private final ParadaMapper paradaMapper;
    private final IRepositorioRuta repositorioRuta;

    @Transactional
    @Auditable(tabla = "parada", operacion = TipoOperacion.CREACION, entidad = Parada.class)
    @CacheEvict(value = "gestion:paradas", allEntries = true)
    public ParadaLiteDto registrarParada(ParadaDto paradaDto){
        Parada parada = paradaMapper.toParada(paradaDto);
        Ruta ruta = repositorioRuta.findById(paradaDto.rutaID())
                .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
        parada.setRuta(ruta);
        Parada paradaGuardada = repositorioParada.save(parada);
        return paradaMapper.toParadaLiteDto(paradaGuardada);
    }

    public ParadaLiteDto buscarPorId(Long id){
        Parada parada = repositorioParada.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la parada con el id " + id));
        return paradaMapper.toParadaLiteDto(parada);
    }

    public List<ParadaSugerenciaDto> sugerirParada(String nombre){
        List<Parada> paradas = repositorioParada.findByNombreContainingIgnoreCase(nombre);
        return paradas.stream()
                .map(paradaMapper::toParadaSugerenciaDto)
                .collect(Collectors.toList());
    }

    public List<ParadaLiteDto> listarTodo(){
        List<Parada> rutas = repositorioParada.findAll();
        return rutas.stream()
                .map(paradaMapper::toParadaLiteDto)
                .collect(Collectors.toList());
    }

    public List<ParadaMapaDto> listarParaMapa(){
        List<Parada> rutas = repositorioParada.findAll();
        return rutas.stream()
                .map(paradaMapper::toParadaMapaDto)
                .collect(Collectors.toList());

    }

    @Transactional
    @Auditable(tabla = "parada", operacion = TipoOperacion.MODIFICACION, entidad = Parada.class)
    @CacheEvict(value = "gestion:paradas", allEntries = true)
    public ParadaLiteDto actualizarParada(Long id, ParadaUpdateDto updateDto){
        Parada parada = repositorioParada.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró la parada con el id " + id));
        if (updateDto.rutaID() != null){
            Ruta ruta = repositorioRuta.findById(updateDto.rutaID())
                    .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
            parada.setRuta(ruta);
        }
        paradaMapper.updateParadaFromDto(updateDto,parada);
        Parada actualizada = repositorioParada.save(parada);
        return paradaMapper.toParadaLiteDto(actualizada);
    }

    /** Devuelve todas las paradas que han sido eliminadas (soft delete). */
    @Transactional
    public List<ParadaLiteDto> listarEliminadas(){
        return repositorioParada.findAllEliminadas().stream()
                .map(paradaMapper::toParadaLiteDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(tabla = "parada", operacion = TipoOperacion.SOFT_DELETE, entidad = Parada.class)
    @CacheEvict(value = "gestion:paradas", allEntries = true)
    public void softDeleteParada(Long id){
        Parada parada = repositorioParada.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Parada no encontrada con id " + id));

        parada.setFechaEliminacion(LocalDateTime.now());
        repositorioParada.save(parada);
    }
}
