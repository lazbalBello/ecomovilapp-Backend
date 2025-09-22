package com.EcoTransporte.GestionSeguimiento.Servicios;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.VehiculoLiteDto;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.VehiculoLiteDtoMapper;
import com.EcoTransporte.GestionSeguimiento.Modelos.Ruta;
import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioRuta;
import com.EcoTransporte.GestionSeguimiento.Repositorios.IRepositorioVehiculo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioAsignarRuta {

    private final IRepositorioRuta repositorioRuta;
    private final IRepositorioVehiculo repositorioVehiculo;
    private final VehiculoLiteDtoMapper vehiculoLiteDtoMapper;

    @Transactional
    public VehiculoLiteDto asignarRutaAVehiculo(Long rutaId , Long vehiculoId){
        Ruta ruta = repositorioRuta.findById(rutaId)
                .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
        Vehiculo vehiculo = repositorioVehiculo.findById(vehiculoId)
                .orElseThrow(()-> new EntityNotFoundException("Vehículo no encontrado"));
        vehiculo.setRuta(ruta);
        if(!ruta.getVehiculosAsignados().contains(vehiculo))
            ruta.getVehiculosAsignados().add(vehiculo);
        Vehiculo actualizado = repositorioVehiculo.save(vehiculo);
        return  vehiculoLiteDtoMapper.toVehiculoLiteDto(actualizado);
    }
}
