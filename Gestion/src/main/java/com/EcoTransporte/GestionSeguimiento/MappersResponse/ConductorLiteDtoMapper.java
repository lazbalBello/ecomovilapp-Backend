package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.ConductorLiteDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Conductor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AsignacionLiteDtoMapper.class})
public interface ConductorLiteDtoMapper {

    ConductorLiteDto toConductorLiteDto(Conductor conductor);
}
