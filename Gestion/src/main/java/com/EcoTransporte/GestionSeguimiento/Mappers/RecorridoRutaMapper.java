package com.EcoTransporte.GestionSeguimiento.Mappers;

import com.EcoTransporte.GestionSeguimiento.Dto.RecorridoRutaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.RecorridoRuta;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RecorridoRutaMapper {
    RecorridoRutaMapper mapper = Mappers.getMapper(RecorridoRutaMapper.class);

    RecorridoRutaDto toRecorridoRutaDto(RecorridoRuta recorridoRuta);

    RecorridoRuta toRecorridoRuta(RecorridoRutaDto recorridoRutaDto);
}
