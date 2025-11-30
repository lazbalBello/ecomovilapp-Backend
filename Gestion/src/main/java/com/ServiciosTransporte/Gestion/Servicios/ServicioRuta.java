package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.Dto.RutaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaLiteDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaSugerenciaDto;
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
    public RutaLiteDto actualizarRuta(Long id, RutaUpdateDto updateDto){
        Ruta ruta = repositorioRuta.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró la ruta con el id " + id));
        rutaUpdateMapper.updateRutaFromDto(updateDto, ruta);
        Ruta actualizada = repositorioRuta.save(ruta);
        return rutaLiteDtoMapper.toRutaDtoResponse(actualizada);
    }

    @Transactional
    public void softDelete(Long id, boolean deleteParadas){
        Ruta ruta = repositorioRuta.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada con id " + id));

        ruta.setFechaEliminacion(LocalDateTime.now());
        repositorioRuta.save(ruta);

        if (deleteParadas){
            repositorioParada.softDeleteFromRuta(id, LocalDateTime.now());
        }
        repositorioVehiculo.desasociarRuta(id);
    }
}
