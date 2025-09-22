package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.ParadaLiteDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParadaLiteDtoMapper {

    @Mapping(target = "ruta", expression = "java(parada.getRuta().getNombre())")
    ParadaLiteDto toParadaLiteDto(Parada parada);
}
