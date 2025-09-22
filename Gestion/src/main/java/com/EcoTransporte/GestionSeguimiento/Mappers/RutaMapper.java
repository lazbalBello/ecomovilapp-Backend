package com.EcoTransporte.GestionSeguimiento.Mappers;

import com.EcoTransporte.GestionSeguimiento.Dto.RutaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Ruta;
import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RutaMapper {
    RutaMapper mapper = Mappers.getMapper(RutaMapper.class);

    RutaDto toRutaDto(Ruta ruta);

    Ruta toRuta(RutaDto rutaDto);
}
