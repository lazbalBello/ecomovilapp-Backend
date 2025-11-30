package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Dto.RecorridoRutaDto;
import com.ServiciosTransporte.Gestion.Modelos.RecorridoRuta;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RecorridoRutaMapper {
    RecorridoRutaMapper mapper = Mappers.getMapper(RecorridoRutaMapper.class);

    RecorridoRutaDto toRecorridoRutaDto(RecorridoRuta recorridoRuta);

    RecorridoRuta toRecorridoRuta(RecorridoRutaDto recorridoRutaDto);
}
