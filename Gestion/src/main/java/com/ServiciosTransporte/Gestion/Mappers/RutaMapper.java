package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Dto.RutaDto;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RutaMapper {
    RutaMapper mapper = Mappers.getMapper(RutaMapper.class);

    RutaDto toRutaDto(Ruta ruta);

    Ruta toRuta(RutaDto rutaDto);
}
