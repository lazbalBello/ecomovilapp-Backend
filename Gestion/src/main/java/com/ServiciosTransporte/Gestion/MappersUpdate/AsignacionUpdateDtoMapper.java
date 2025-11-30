package com.ServiciosTransporte.Gestion.MappersUpdate;

import com.ServiciosTransporte.Gestion.DtoUpdate.AsignacionUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AsignacionUpdateDtoMapper {

    void updateAsignacionFromDto(AsignacionUpdateDto updateDto, @MappingTarget VehiculoAsignacion asignacion);
}
