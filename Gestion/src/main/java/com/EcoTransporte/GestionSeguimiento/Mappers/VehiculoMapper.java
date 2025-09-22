package com.EcoTransporte.GestionSeguimiento.Mappers;

import com.EcoTransporte.GestionSeguimiento.Dto.VehiculoDto;
import com.EcoTransporte.GestionSeguimiento.MappersResponse.RutaLiteDtoMapper;
import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
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
