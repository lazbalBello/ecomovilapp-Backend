package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.ConductorSugerenciaDto;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConductorSugerenciaDtoMapper {

    @Mapping(target = "nombre" , expression = "java(conductor.getNombre() + \" \" + conductor.getApellidos())")
    ConductorSugerenciaDto toConductorSugerenciaDto(Conductor conductor);
}
