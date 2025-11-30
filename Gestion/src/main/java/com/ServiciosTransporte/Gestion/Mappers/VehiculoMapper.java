package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Dto.VehiculoDto;
import com.ServiciosTransporte.Gestion.MappersResponse.RutaLiteDtoMapper;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring", uses = {RutaLiteDtoMapper.class})
public interface VehiculoMapper {
    VehiculoMapper mapper = Mappers.getMapper(VehiculoMapper.class);

    VehiculoDto toVehiculoDto(Vehiculo vehiculo);

    @Mapping(target = "ruta", ignore = true)
    Vehiculo toVehiculo(VehiculoDto vehiculoDto);
}
