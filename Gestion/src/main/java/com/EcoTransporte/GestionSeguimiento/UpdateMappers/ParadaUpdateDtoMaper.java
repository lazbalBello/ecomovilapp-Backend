package com.EcoTransporte.GestionSeguimiento.UpdateMappers;

import com.EcoTransporte.GestionSeguimiento.DtoUpdate.ParadaUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ParadaUpdateDtoMaper {

    void updateParadaFromDto(ParadaUpdateDto updateDto, @MappingTarget Parada parada);
}
