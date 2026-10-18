package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.Dto.ParadaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Paradas.ParadaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.ParadaUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CentralMapperConfig.class)
public interface ParadaMapper {

    ParadaDto toParadaDto(Parada parada);

    Parada toParada(ParadaDto paradaDto);

    @Mapping(target = "ruta", expression = "java(parada.getRuta().getNombre())")
    ParadaLiteDto toParadaLiteDto(Parada parada);

    ParadaMapaDto toParadaMapaDto(Parada parada);

    ParadaSugerenciaDto toParadaSugerenciaDto(Parada parada);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateParadaFromDto(ParadaUpdateDto updateDto, @MappingTarget Parada parada);
}
