package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParadaMapaDtoMapper {

    @Mapping(target = "ruta", expression = "java(parada.getRuta().getNombre())")
    ParadaMapaDto toParadaMapaDto(Parada parada);
}
