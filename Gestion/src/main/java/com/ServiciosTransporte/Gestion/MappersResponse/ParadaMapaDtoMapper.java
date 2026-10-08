package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.Paradas.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParadaMapaDtoMapper {

    ParadaMapaDto toParadaMapaDto(Parada parada);
}
