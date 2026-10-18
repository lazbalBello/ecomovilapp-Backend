package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Auditoria.Anotaciones.Auditable;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Dto.RutaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.RutaUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.RutaMapper;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioParada;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ServicioRuta {

    private final IRepositorioRuta repositorioRuta;
    private final RutaMapper rutaMapper;
    private final IRepositorioParada repositorioParada;
    private final IRepositorioVehiculo repositorioVehiculo;

    @Transactional
    @Auditable(tabla = "ruta", operacion = TipoOperacion.CREACION, entidad = Ruta.class)
    @Caching(evict = {
            @CacheEvict(value = "gestion:rutas", allEntries = true),
            @CacheEvict(value = "gestion:resumen-activos", allEntries = true)
    })
    public RutaDto registrarRuta(RutaDto rutaDto){
        Ruta ruta = rutaMapper.toRuta(rutaDto);
        Ruta rutaGuardada = repositorioRuta.save(ruta);
        return rutaMapper.toRutaDto(rutaGuardada);
    }

   public RutaLiteDto buscarPorId(Long id){
        Ruta ruta = repositorioRuta.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ruta con id " + id + " no encontrada"));
        return rutaMapper.toRutaDtoResponse(ruta);
   }

   public List<RutaSugerenciaDto> sugerirRuta(String nombre){
       List<Ruta> rutas = repositorioRuta.findByNombreContainingIgnoreCase(nombre);
       return  rutas.stream()
               .map(rutaMapper::toRutaSugerenciaDto)
               .collect(Collectors.toList());
   }

    public List<RutaLiteDto> listarTodo(){
        List<Ruta> rutas = repositorioRuta.findAll();
        return rutas.stream()
                .map(rutaMapper::toRutaDtoResponse)
                .collect(Collectors.toList());
    }

    public List<RutaMapaDto> rutasParaMapa(){
        List<Ruta> rutas = repositorioRuta.findAll();
        return rutas.stream()
                .map(rutaMapper::toRutaMapaDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(tabla = "ruta", operacion = TipoOperacion.MODIFICACION, entidad = Ruta.class)
    @CacheEvict(value = "gestion:rutas", allEntries = true)
    public RutaLiteDto actualizarRuta(Long id, RutaUpdateDto updateDto){
        Ruta ruta = repositorioRuta.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró la ruta con el id " + id));
        rutaMapper.updateRutaFromDto(updateDto, ruta);
        Ruta actualizada = repositorioRuta.save(ruta);
        return rutaMapper.toRutaDtoResponse(actualizada);
    }

    /** Devuelve todas las rutas que han sido eliminadas (soft delete). */
    @Transactional
    public List<RutaLiteDto> listarEliminadas(){
        return repositorioRuta.findAllEliminadas().stream()
                .map(rutaMapper::toRutaDtoResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(tabla = "ruta", operacion = TipoOperacion.SOFT_DELETE, entidad = Ruta.class)
    @Caching(evict = {
            @CacheEvict(value = "gestion:rutas", allEntries = true),
            @CacheEvict(value = "gestion:paradas", allEntries = true),
            @CacheEvict(value = "gestion:resumen-activos", allEntries = true)
    })
    public void softDelete(Long id){
        Ruta ruta = repositorioRuta.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada con id " + id));

        ruta.setFechaEliminacion(LocalDateTime.now());
        repositorioRuta.save(ruta);
        repositorioParada.softDeleteFromRuta(id, LocalDateTime.now());
        repositorioVehiculo.desasociarRuta(id);
    }
}
