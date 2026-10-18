package com.ServiciosTransporte.Gestion.MappersUpdate;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.DtoActualizacion.RutaUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CentralMapperConfig.class, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RutaUpdateMapper {

    void updateRutaFromDto(RutaUpdateDto updateDto, @MappingTarget Ruta ruta);
}
