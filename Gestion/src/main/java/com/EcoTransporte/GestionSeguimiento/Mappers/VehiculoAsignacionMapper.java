package com.EcoTransporte.GestionSeguimiento.Mappers;

import com.EcoTransporte.GestionSeguimiento.Dto.VehiculoAsignacionDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.VehiculoAsignacion;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VehiculoAsignacionMapper {
    VehiculoAsignacionMapper mapper = Mappers.getMapper(VehiculoAsignacionMapper.class);

    VehiculoAsignacionDto toVehiculoAsignacionDto(VehiculoAsignacion vehiculoAsignacion);

    VehiculoAsignacion toVehiculoAsignacion(VehiculoAsignacionDto vehiculoAsignacionDto);
}
