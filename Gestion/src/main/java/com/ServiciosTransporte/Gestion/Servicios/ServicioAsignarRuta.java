package com.ServiciosTransporte.Gestion.Servicios;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Mappers.VehiculoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioRuta;
import com.ServiciosTransporte.Gestion.Repositorios.IRepositorioVehiculo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServicioAsignarRuta {

    private final IRepositorioRuta repositorioRuta;
    private final IRepositorioVehiculo repositorioVehiculo;
    private final VehiculoMapper vehiculoMapper;

    @Transactional
    @CacheEvict(value = "gestion:resumen-activos", allEntries = true)
    public VehiculoLiteDto asignarRutaAVehiculo(Long rutaId , Long vehiculoId){
        Ruta ruta = repositorioRuta.findById(rutaId)
                .orElseThrow(()-> new EntityNotFoundException("Ruta no encontrada"));
        Vehiculo vehiculo = repositorioVehiculo.findById(vehiculoId)
                .orElseThrow(()-> new EntityNotFoundException("Vehículo no encontrado"));
        vehiculo.setRuta(ruta);
        if(!ruta.getVehiculosAsignados().contains(vehiculo))
            ruta.getVehiculosAsignados().add(vehiculo);
        Vehiculo actualizado = repositorioVehiculo.save(vehiculo);
        return  vehiculoMapper.toVehiculoLiteDto(actualizado);
    }
}
