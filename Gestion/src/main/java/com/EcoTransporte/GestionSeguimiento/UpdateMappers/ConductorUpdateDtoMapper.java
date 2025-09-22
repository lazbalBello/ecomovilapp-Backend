package com.EcoTransporte.GestionSeguimiento.UpdateMappers;

import com.EcoTransporte.GestionSeguimiento.DtoUpdate.ConductorUpdateDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Conductor;
import org.mapstruct.*;

import java.util.HashSet;
import java.util.Set;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConductorUpdateDtoMapper {

    @Mapping(target = "categoriasLicencia", ignore = true)
    void updateConductorFromDto(ConductorUpdateDto updateDto, @MappingTarget Conductor conductor);
}
