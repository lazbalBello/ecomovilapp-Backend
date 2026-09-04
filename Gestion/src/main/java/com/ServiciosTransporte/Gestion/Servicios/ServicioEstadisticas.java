package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoResponse.EstadisticasSemanalesDto;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioConductor;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ServicioEstadisticas {

    private final IRepositorioVehiculo repositorioVehiculo;
    private final IRepositorioConductor repositorioConductor;
    private final IRepositorioRuta repositorioRuta;

    public EstadisticasSemanalesDto obtenerEstadisticasSemanales() {
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        return new EstadisticasSemanalesDto(
                repositorioVehiculo.countCreadosDesde(desde),
                repositorioConductor.countCreadosDesde(desde),
                repositorioRuta.countCreadasDesde(desde)
        );
    }
}
