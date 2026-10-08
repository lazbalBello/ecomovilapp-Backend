package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Auditoria.Anotaciones.Auditable;
import com.ServiciosTransporte.Gestion.Auditoria.Modelos.TipoOperacion;
import com.ServiciosTransporte.Gestion.Dto.RutaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Rutas.RutaLiteDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Rutas.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.Rutas.RutaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoUpdate.RutaUpdateDto;
import com.ServiciosTransporte.Gestion.Mappers.RutaMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.RutaLiteDtoMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.RutaMapaDtoMapper;
import com.ServiciosTransporte.Gestion.MappersResponse.RutaSugerenciaDtoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioParada;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.MappersUpdate.RutaUpdateMapper;
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
    private final RutaLiteDtoMapper rutaLiteDtoMapper;
    private final RutaSugerenciaDtoMapper rutaSugerenciaDtoMapper;
    private final RutaUpdateMapper rutaUpdateMapper;
    private final IRepositorioParada repositorioParada;
    private final IRepositorioVehiculo repositorioVehiculo;
    private final RutaMapaDtoMapper rutaMapaDtoMapper;

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
        return rutaLiteDtoMapper.toRutaDtoResponse(ruta);
   }

   public List<RutaSugerenciaDto> sugerirRuta(String nombre){
       List<Ruta> rutas = repositorioRuta.findByNombreContainingIgnoreCase(nombre);
       return  rutas.stream()
               .map(rutaSugerenciaDtoMapper::toRutaSugerenciaDto)
               .collect(Collectors.toList());
   }

    public List<RutaLiteDto> listarTodo(){
        List<Ruta> rutas = repositorioRuta.findAll();
        return rutas.stream()
                .map(rutaLiteDtoMapper::toRutaDtoResponse)
                .collect(Collectors.toList());
    }

    public List<RutaMapaDto> rutasParaMapa(){
        List<Ruta> rutas = repositorioRuta.findAll();
        return rutas.stream()
                .map(rutaMapaDtoMapper::toRutaMapaDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Auditable(tabla = "ruta", operacion = TipoOperacion.MODIFICACION, entidad = Ruta.class)
    @CacheEvict(value = "gestion:rutas", allEntries = true)
    public RutaLiteDto actualizarRuta(Long id, RutaUpdateDto updateDto){
        Ruta ruta = repositorioRuta.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró la ruta con el id " + id));
        rutaUpdateMapper.updateRutaFromDto(updateDto, ruta);
        Ruta actualizada = repositorioRuta.save(ruta);
        return rutaLiteDtoMapper.toRutaDtoResponse(actualizada);
    }

    /** Devuelve todas las rutas que han sido eliminadas (soft delete). */
    @Transactional
    public List<RutaLiteDto> listarEliminadas(){
        return repositorioRuta.findAllEliminadas().stream()
                .map(rutaLiteDtoMapper::toRutaDtoResponse)
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
