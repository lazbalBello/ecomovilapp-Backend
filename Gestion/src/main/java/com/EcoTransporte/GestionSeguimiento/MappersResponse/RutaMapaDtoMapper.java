package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.RutaMapaDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Ruta;
import com.EcoTransporte.GestionSeguimiento.Modelos.Vehiculo;
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
