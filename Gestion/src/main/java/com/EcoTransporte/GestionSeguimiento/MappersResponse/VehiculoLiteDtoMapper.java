package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.VehiculoLiteDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RutaLiteDtoMapper.class, AsignacionLiteDtoMapper.class})
public interface VehiculoLiteDtoMapper {

    VehiculoLiteDto toVehiculoLiteDto(Vehiculo vehiculo);
}
