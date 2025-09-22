package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.RutaSugerenciaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Ruta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RutaSugerenciaDtoMapper {
    RutaSugerenciaDto toRutaSugerenciaDto(Ruta ruta);
}
