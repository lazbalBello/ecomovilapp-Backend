package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.ParadaLiteDto;
import com.EcoTransporte.GestionSeguimiento.DtoResponse.ParadaMapaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParadaMapaDtoMapper {

    @Mapping(target = "ruta", expression = "java(parada.getRuta().getNombre())")
    ParadaMapaDto toParadaMapaDto(Parada parada);
}
