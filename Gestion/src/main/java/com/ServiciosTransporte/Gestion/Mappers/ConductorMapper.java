package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Dto.ConductorDto;
import com.ServiciosTransporte.Gestion.Dto.ConductorUsuarioDto;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ConductorMapper {
    ConductorMapper mapper = Mappers.getMapper(ConductorMapper.class);

    ConductorDto toConductorDto(Conductor conductor);

    @Mapping(target = "id", ignore = true)
    Conductor toConductor(ConductorDto conductorDto);

    @Mapping(target = "id", ignore = true)
    Conductor toConductor(ConductorUsuarioDto conductorUsuarioDto);
}
