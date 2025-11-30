package com.ServiciosTransporte.Gestion.MappersUpdate;

import com.ServiciosTransporte.Gestion.DtoUpdate.ConductorUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConductorUpdateDtoMapper {

    @Mapping(target = "categoriasLicencia", ignore = true)
    void updateConductorFromDto(ConductorUpdateDto updateDto, @MappingTarget Conductor conductor);
}
