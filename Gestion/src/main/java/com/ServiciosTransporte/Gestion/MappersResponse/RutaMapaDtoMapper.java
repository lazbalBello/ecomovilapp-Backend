package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.RutaMapaDto;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = ParadaLiteDtoMapper.class)
public interface RutaMapaDtoMapper {

    RutaMapaDto toRutaMapaDto(Ruta ruta);

}
