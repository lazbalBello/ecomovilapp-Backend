package com.EcoTransporte.GestionSeguimiento.Mappers;

import com.EcoTransporte.GestionSeguimiento.Dto.ParadaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ParadaMapper {
    ParadaMapper mapper = Mappers.getMapper(ParadaMapper.class);

    ParadaDto toParadaDto(Parada parada);

    Parada toParada(ParadaDto paradaDto);
}
