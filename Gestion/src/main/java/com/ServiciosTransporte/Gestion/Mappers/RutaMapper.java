package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.Dto.RutaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaMapaDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Rutas.RutaSugerenciaDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.RutaUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Ruta;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = CentralMapperConfig.class, uses = {ParadaMapper.class})
public interface RutaMapper {

    RutaDto toRutaDto(Ruta ruta);

    @Mapping(target = "id", ignore = true)
    Ruta toRuta(RutaDto rutaDto);

    @Mapping(target = "vehiculosAsignados", expression = "java( mapVehiculosAsignados(ruta.getVehiculosAsignados()) )")
    RutaLiteDto toRutaDtoResponse(Ruta ruta);

    default List<String> mapVehiculosAsignados(List<Vehiculo> vehiculos) {
        return vehiculos == null
                ? null
                : vehiculos.stream()
                .map(Vehiculo::getMatricula)
                .collect(Collectors.toList());
    }

    default List<RutaLiteDto> toResponseDtoList(List<Ruta> rutas) {
        return rutas == null
                ? null
                : rutas.stream()
                .map(this::toRutaDtoResponse)
                .collect(Collectors.toList());
    }

    RutaMapaDto toRutaMapaDto(Ruta ruta);

    RutaSugerenciaDto toRutaSugerenciaDto(Ruta ruta);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRutaFromDto(RutaUpdateDto updateDto, @MappingTarget Ruta ruta);
}
