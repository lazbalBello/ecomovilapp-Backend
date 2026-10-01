package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParadaMapaDtoMapper {

    ParadaMapaDto toParadaMapaDto(Parada parada);
}
