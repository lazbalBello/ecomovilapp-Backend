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

    @Mapping(target = "vehiculosAsignados", expression = "java( mapVehiculosAsignados(ruta.getVehiculosAsignados()) )")
    RutaMapaDto toRutaMapaDto(Ruta ruta);

    default List<String> mapVehiculosAsignados(List<Vehiculo> vehiculos) {
        return vehiculos == null
                ? null
                : vehiculos.stream()
                .map(Vehiculo::getMatricula)
                .collect(Collectors.toList());
    }

    default List<RutaMapaDto> toResponseDtoList(List<Ruta> rutas) {
        return rutas == null
                ? null
                : rutas.stream()
                .map(this::toRutaMapaDto)
                .collect(Collectors.toList());
    }
}
