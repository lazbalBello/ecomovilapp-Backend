package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaSugerenciaDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ParadaSugerenciaDtoMapper {

    ParadaSugerenciaDto toParadaSugerenciaDto(Parada parada);
}
