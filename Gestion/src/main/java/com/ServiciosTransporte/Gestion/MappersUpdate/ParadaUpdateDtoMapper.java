package com.ServiciosTransporte.Gestion.MappersUpdate;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.DtoActualizacion.ParadaUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Parada;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CentralMapperConfig.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ParadaUpdateDtoMapper {

    void updateParadaFromDto(ParadaUpdateDto updateDto, @MappingTarget Parada parada);
}
