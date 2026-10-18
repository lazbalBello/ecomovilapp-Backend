package com.ServiciosTransporte.Gestion.MappersUpdate;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.DtoActualizacion.VehiculoUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.mapstruct.*;

@Mapper(config = CentralMapperConfig.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehiculoUpdateMapper {

    @Mapping(target = "estado", source = "estado", qualifiedByName = "integerToEstado")
    void updateVehiculoFromDto(VehiculoUpdateDto updateDto, @MappingTarget Vehiculo vehiculo);

    @Named("integerToEstado")
    default EstadoVehiculo integerToEstado(Integer valor){
        if(valor == null)
            return null;

        if (valor < 0 || valor >= EstadoVehiculo.values().length) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Índice de estado de vehículo no válido: " + valor
            );
        }

        return EstadoVehiculo.values()[valor];
    }
}
