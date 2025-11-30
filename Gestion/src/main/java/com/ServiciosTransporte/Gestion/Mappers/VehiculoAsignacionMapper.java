package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Dto.VehiculoAsignacionDto;
import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VehiculoAsignacionMapper {
    VehiculoAsignacionMapper mapper = Mappers.getMapper(VehiculoAsignacionMapper.class);

    VehiculoAsignacionDto toVehiculoAsignacionDto(VehiculoAsignacion vehiculoAsignacion);

    VehiculoAsignacion toVehiculoAsignacion(VehiculoAsignacionDto vehiculoAsignacionDto);
}
