package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.Dto.ConductorDto;
import com.ServiciosTransporte.Gestion.Dto.ConductorUsuarioDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Conductores.ConductorLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Conductores.ConductorSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.ConductorUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CentralMapperConfig.class, uses = {VehiculoAsignacionMapper.class})
public interface ConductorMapper {

    ConductorDto toConductorDto(Conductor conductor);

    @Mapping(target = "id", ignore = true)
    Conductor toConductor(ConductorDto conductorDto);

    @Mapping(target = "id", ignore = true)
    Conductor toConductor(ConductorUsuarioDto conductorUsuarioDto);

    ConductorLiteDto toConductorLiteDto(Conductor conductor);

    @Mapping(target = "nombre", expression = "java(conductor.getNombre() + \" \" + conductor.getApellidos())")
    ConductorSugerenciaDto toConductorSugerenciaDto(Conductor conductor);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "categoriasLicencia", ignore = true)
    void updateConductorFromDto(ConductorUpdateDto updateDto, @MappingTarget Conductor conductor);
}
