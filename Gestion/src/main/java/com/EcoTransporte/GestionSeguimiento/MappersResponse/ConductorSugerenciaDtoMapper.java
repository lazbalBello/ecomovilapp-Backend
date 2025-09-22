package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.ConductorSugerenciaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Conductor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConductorSugerenciaDtoMapper {

    @Mapping(target = "nombre" , expression = "java(conductor.getNombre() + \" \" + conductor.getApellidos())")
    ConductorSugerenciaDto toConductorSugerenciaDto(Conductor conductor);
}
