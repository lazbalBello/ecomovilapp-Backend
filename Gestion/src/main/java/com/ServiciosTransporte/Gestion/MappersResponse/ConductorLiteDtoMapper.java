package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.Conductores.ConductorLiteDto;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AsignacionLiteDtoMapper.class})
public interface ConductorLiteDtoMapper {

    ConductorLiteDto toConductorLiteDto(Conductor conductor);
}
