package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.Rutas.RutaMapaDto;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ParadaLiteDtoMapper.class)
public interface RutaMapaDtoMapper {

    RutaMapaDto toRutaMapaDto(Ruta ruta);

}
