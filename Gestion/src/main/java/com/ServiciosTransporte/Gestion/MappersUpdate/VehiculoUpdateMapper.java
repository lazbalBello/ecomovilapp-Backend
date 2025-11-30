package com.ServiciosTransporte.Gestion.MappersUpdate;

import com.ServiciosTransporte.Gestion.DtoUpdate.VehiculoUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
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
