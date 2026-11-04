package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Estadisticas.PublicoEstadisticasDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoPublicoDto;
import com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoMapper;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicioEstadisticasPublicas {

    private final IRepositorioVehiculo repositorioVehiculo;
    private final IRepositorioRuta repositorioRuta;
    private final ServicioRuta servicioRuta;
    private final ServicioParada servicioParada;
    private final VehiculoMapper vehiculoMapper;

    @Cacheable(value = "gestion:vehiculos", key = "'todos'")
    public List<VehiculoPublicoDto> listarVehiculosPublico(){
        List<Vehiculo> vehiculos = repositorioVehiculo.findAll();
        return vehiculos.stream()
                .map(vehiculoMapper::toVehiculoPublicoDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "gestion:resumen-activos", key = "'resumen'")
    public PublicoEstadisticasDto obtenerEstadisticasPublicas() {
        return new PublicoEstadisticasDto(
                repositorioVehiculo.countByEstado(EstadoVehiculo.ACTIVO),
                repositorioRuta.countRutasActivasConVehiculosActivos()
        );
    }

    @Cacheable(value = "gestion:rutas", key = "'todas'")
    public List<RutaMapaDto> listarRutasParaMapa() {
        return servicioRuta.rutasParaMapa();
    }

    @Cacheable(value = "gestion:paradas", key = "'todas'")
    public List<ParadaMapaDto> listarParadasParaMapa() {
        return servicioParada.listarParaMapa();
    }
}