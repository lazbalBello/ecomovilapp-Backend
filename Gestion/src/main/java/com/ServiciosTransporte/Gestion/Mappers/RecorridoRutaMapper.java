package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.Dto.RecorridoRutaDto;
import com.ServiciosTransporte.Gestion.Modelos.RecorridoRuta;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface RecorridoRutaMapper {

    RecorridoRutaDto toRecorridoRutaDto(RecorridoRuta recorridoRuta);

    RecorridoRuta toRecorridoRuta(RecorridoRutaDto recorridoRutaDto);
}
