package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoResponse.PublicoEstadisticasDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioParada;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicoService {

    private final IRepositorioVehiculo repositorioVehiculo;
    private final IRepositorioRuta repositorioRuta;
    private final ServicioRuta servicioRuta;
    private final ServicioParada servicioParada;

    @Cacheable(value = "gestion:resumen-activos", key = "'resumen'")
    public PublicoEstadisticasDto obtenerEstadisticasPublicas() {
        return new PublicoEstadisticasDto(
                repositorioVehiculo.countVehiculosActivos(),
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