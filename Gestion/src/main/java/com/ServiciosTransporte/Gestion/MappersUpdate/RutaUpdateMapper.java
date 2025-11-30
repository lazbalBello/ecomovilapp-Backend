package com.ServiciosTransporte.Gestion.MappersUpdate;

import com.ServiciosTransporte.Gestion.DtoUpdate.RutaUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RutaUpdateMapper {

    void updateRutaFromDto(RutaUpdateDto updateDto, @MappingTarget Ruta ruta);
}
