package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.ParadaLiteDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParadaLiteDtoMapper {

    @Mapping(target = "ruta", expression = "java(parada.getRuta().getNombre())")
    ParadaLiteDto toParadaLiteDto(Parada parada);
}
