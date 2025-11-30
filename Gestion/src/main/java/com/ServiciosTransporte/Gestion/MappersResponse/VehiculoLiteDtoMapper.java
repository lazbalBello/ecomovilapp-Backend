package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {RutaLiteDtoMapper.class, AsignacionLiteDtoMapper.class})
public interface VehiculoLiteDtoMapper {

    VehiculoLiteDto toVehiculoLiteDto(Vehiculo vehiculo);
}
