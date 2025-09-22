package com.EcoTransporte.GestionSeguimiento.Mappers;

import com.EcoTransporte.GestionSeguimiento.Dto.ConductorDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Conductor;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ConductorMapper {
    ConductorMapper mapper = Mappers.getMapper(ConductorMapper.class);

    ConductorDto toConductorDto(Conductor conductor);

    Conductor toConductor(ConductorDto conductorDto);
}
