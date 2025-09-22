package com.EcoTransporte.GestionSeguimiento.Servicios;

import com.EcoTransporte.GestionSeguimiento.Dto.ParadaDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.ParadaLiteDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.ParadaMapaDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.ParadaSugerenciaDto;
import com.EcoTransporte.GestionSeguimiento.DtoUpdate.ParadaUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Mappers.ParadaMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.ParadaLiteDtoMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.ParadaMapaDtoMapper;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.ParadaSugerenciaDtoMapper;
import com.EcoTransporte.GestionSeguimiento.Modelos.Parada;
import com.EcoTransporte.GestionSeguimiento.Modelos.Ruta;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioParada;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioRuta;
import com.EcoTransporte.GestionSeguimiento.UpdateMappers.ParadaUpdateDtoMaper;
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
public class ServicioParada {

    private final IRepositorioParada repositorioParada;
    private final ParadaMapper paradaMapper;
    private final ParadaLiteDtoMapper paradaLiteDtoMapper;
    private final IRepositorioRuta repositorioRuta;
    private final ParadaSugerenciaDtoMapper paradaSugerenciaDtoMapper;
    private final ParadaUpdateDtoMaper paradaUpdateDtoMaper;
    private final ParadaMapaDtoMapper paradaMapaDtoMapper;

    @Transactional
    public ParadaLiteDto registrarParada(ParadaDto paradaDto){
        Parada parada = paradaMapper.toParada(paradaDto);
        Ruta ruta = repositorioRuta.findById(paradaDto.getRutaID())
                .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
        parada.setRuta(ruta);
        Parada paradaGuardada = repositorioParada.save(parada);
        return paradaLiteDtoMapper.toParadaLiteDto(paradaGuardada);
    }

    public ParadaLiteDto buscarPorId(Long id){
        Parada parada = repositorioParada.findById(id)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró la parada con el id " + id));
        return paradaLiteDtoMapper.toParadaLiteDto(parada);
    }

    public List<ParadaSugerenciaDto> sugerirParada(String nombre){
        List<Parada> paradas = repositorioParada.findByNombreContainingIgnoreCase(nombre);
        return paradas.stream()
                .map(paradaSugerenciaDtoMapper::toParadaSugerenciaDto)
                .collect(Collectors.toList());
    }

    public List<ParadaLiteDto> listarTodo(){
        List<Parada> rutas = repositorioParada.findAll();
        return rutas.stream()
                .map(paradaLiteDtoMapper::toParadaLiteDto)
                .collect(Collectors.toList());
    }

    public List<ParadaMapaDto> listarParaMapa(){
        List<Parada> rutas = repositorioParada.findAll();
        return rutas.stream()
                .map(paradaMapaDtoMapper::toParadaMapaDto)
                .collect(Collectors.toList());

    }

    @Transactional
    public ParadaLiteDto actualizarParada(Long id, ParadaUpdateDto updateDto){
        Parada parada = repositorioParada.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("No se encontró la parada con el id " + id));
        if (updateDto.getRutaID() != null){
            Ruta ruta = repositorioRuta.findById(updateDto.getRutaID())
                    .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
            parada.setRuta(ruta);
        }
        paradaUpdateDtoMaper.updateParadaFromDto(updateDto,parada);
        Parada actualizada = repositorioParada.save(parada);
        return paradaLiteDtoMapper.toParadaLiteDto(actualizada);
    }

    @Transactional
    public void softDeleteParada(Long id){
        Parada parada = repositorioParada.findById(id)
                .orElseThrow(()-> new EntityNotFoundException("Parada no encontrada con id " + id));

        parada.setFechaEliminacion(LocalDateTime.now());
        repositorioParada.save(parada);
    }
}
