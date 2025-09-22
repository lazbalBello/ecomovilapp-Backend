package com.EcoTransporte.GestionSeguimiento.UpdateMappers;

import com.EcoTransporte.GestionSeguimiento.DtoUpdate.VehiculoUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.EstadoVehiculo;
import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehiculoUpdateMapper {

    @Mapping(target = "estado", source = "estado", qualifiedByName = "integerToEstado")
    void updateVehiculoFromDto(VehiculoUpdateDto updateDto, @MappingTarget Vehiculo vehiculo);

    @Named("integerToEstado")
    default EstadoVehiculo integerToEstado(Integer valor){
        if(valor == null)
            return null;

        return EstadoVehiculo.values()[valor];
    }
}
