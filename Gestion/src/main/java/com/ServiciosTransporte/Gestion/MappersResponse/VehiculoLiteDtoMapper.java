package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoPublicoDto;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class, uses = {RutaLiteDtoMapper.class, AsignacionLiteDtoMapper.class})
public interface VehiculoLiteDtoMapper {

    VehiculoLiteDto toVehiculoLiteDto(Vehiculo vehiculo);

    VehiculoPublicoDto toVehiculoPublicoDto(Vehiculo vehiculo);
}
