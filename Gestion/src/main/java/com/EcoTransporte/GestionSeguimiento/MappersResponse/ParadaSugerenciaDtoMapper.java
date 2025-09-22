package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.ParadaSugerenciaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Parada;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParadaSugerenciaDtoMapper {

    ParadaSugerenciaDto toParadaSugerenciaDto(Parada parada);
}
