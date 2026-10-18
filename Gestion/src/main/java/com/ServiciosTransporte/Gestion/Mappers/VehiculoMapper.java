package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.Dto.VehiculoDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoLiteDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Vehiculos.VehiculoPublicoDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.VehiculoUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.EstadoVehiculo;
import com.ServiciosTransporte.Gestion.Modelos.Vehiculo;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CentralMapperConfig.class, uses = {RutaMapper.class, VehiculoAsignacionMapper.class})
public interface VehiculoMapper {

    VehiculoDto toVehiculoDto(Vehiculo vehiculo);

    @Mapping(target = "ruta", ignore = true)
    @Mapping(target = "id", ignore = true)
    Vehiculo toVehiculo(VehiculoDto vehiculoDto);

    VehiculoLiteDto toVehiculoLiteDto(Vehiculo vehiculo);

    VehiculoPublicoDto toVehiculoPublicoDto(Vehiculo vehiculo);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "estado", source = "estado", qualifiedByName = "integerToEstado")
    void updateVehiculoFromDto(VehiculoUpdateDto updateDto, @MappingTarget Vehiculo vehiculo);

    @Named("integerToEstado")
    default EstadoVehiculo integerToEstado(Integer valor){
        if(valor == null)
            return null;

        if (valor < 0 || valor >= EstadoVehiculo.values().length) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Índice de estado de vehículo no válido: " + valor
            );
        }

        return EstadoVehiculo.values()[valor];
    }
}
