package com.EcoTransporte.GestionSeguimiento.UpdateMappers;

import com.EcoTransporte.GestionSeguimiento.DtoUpdate.AsignacionUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.VehiculoAsignacion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AsignacionUpdateDtoMapper {

    void updateAsignacionFromDto(AsignacionUpdateDto updateDto, @MappingTarget VehiculoAsignacion asignacion);
}
