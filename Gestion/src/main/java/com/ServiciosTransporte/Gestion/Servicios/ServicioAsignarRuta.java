package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.MappersResponse.VehiculoLiteDtoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
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
