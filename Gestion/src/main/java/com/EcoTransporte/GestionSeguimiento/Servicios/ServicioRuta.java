package com.EcoTransporte.GestionSeguimiento.Servicios;

import com.EcoTransporte.GestionSeguimiento.Dto.RutaDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.RutaLiteDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.RutaMapaDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.RutaSugerenciaDto;
import com.EcoTransporte.GestionSeguimiento.DtoUpdate.RutaUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Mappers.RutaMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.RutaLiteDtoMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.RutaMapaDtoMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.RutaSugerenciaDtoMapper;
import com.EcoTransporte.GestionSeguimiento.Modelos.Ruta;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioParada;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioRuta;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioVehiculo;
import com.EcoTransporte.GestionSeguimiento.UpdateMappers.RutaUpdateMapper;
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
