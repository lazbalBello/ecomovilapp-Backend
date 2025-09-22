package com.EcoTransporte.GestionSeguimiento.UpdateMappers;

import com.EcoTransporte.GestionSeguimiento.DtoUpdate.RutaUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Ruta;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RutaUpdateMapper {

    void updateRutaFromDto(RutaUpdateDto updateDto, @MappingTarget Ruta ruta);
}
