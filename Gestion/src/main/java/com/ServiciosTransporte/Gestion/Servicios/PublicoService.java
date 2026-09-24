package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoResponse.PublicoEstadisticasDto;
import com.ServiciosTransporte.Gestion.DtoResponse.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoResponse.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioParada;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicoService {

    private final IRepositorioVehiculo repositorioVehiculo;
    private final IRepositorioRuta repositorioRuta;
    private final IRepositorioParada repositorioParada;
    private final ServicioRuta servicioRuta;
    private final ServicioParada servicioParada;

    public PublicoEstadisticasDto obtenerEstadisticasPublicas() {
        return new PublicoEstadisticasDto(
                repositorioVehiculo.countVehiculosActivos(),
                repositorioRuta.countRutasActivasConVehiculosActivos()
        );
    }

    public List<RutaMapaDto> listarRutasParaMapa() {
        return servicioRuta.rutasParaMapa();
    }

    public List<ParadaMapaDto> listarParadasParaMapa() {
        return servicioParada.listarParaMapa();
    }
}