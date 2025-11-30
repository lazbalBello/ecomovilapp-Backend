package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.RutaSugerenciaDto;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RutaSugerenciaDtoMapper {
    RutaSugerenciaDto toRutaSugerenciaDto(Ruta ruta);
}
