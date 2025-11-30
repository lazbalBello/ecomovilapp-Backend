package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Dto.ParadaDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ParadaMapper {
    ParadaMapper mapper = Mappers.getMapper(ParadaMapper.class);

    ParadaDto toParadaDto(Parada parada);

    Parada toParada(ParadaDto paradaDto);
}
