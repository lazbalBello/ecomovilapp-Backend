package com.ServiciosTransporte.Gestion.Mappers;

import com.ServiciosTransporte.Gestion.Config.CentralMapperConfig;
import com.ServiciosTransporte.Gestion.Dto.VehiculoAsignacionDto;
import com.ServiciosTransporte.Gestion.DtoRespuesta.Asignaciones.AsignacionLiteDto;
import com.ServiciosTransporte.Gestion.DtoActualizacion.AsignacionUpdateDto;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(config = CentralMapperConfig.class)
public interface VehiculoAsignacionMapper {

    VehiculoAsignacionDto toVehiculoAsignacionDto(VehiculoAsignacion vehiculoAsignacion);

    VehiculoAsignacion toVehiculoAsignacion(VehiculoAsignacionDto vehiculoAsignacionDto);

    @Mapping(source = "conductor.dni", target = "dniConductor")
    @Mapping(source = "vehiculo.matricula", target = "matriculaVehiculo")
    @Mapping(target = "conductor", expression = "java(nombreConductor(vehiculoAsignacion.getConductor()))")
    AsignacionLiteDto toAsignacionLiteDto(VehiculoAsignacion vehiculoAsignacion);

    default String nombreConductor(Conductor conductor){
        return (conductor != null) ? conductor.getNombre() + " " + conductor.getApellidos()
                : null;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAsignacionFromDto(AsignacionUpdateDto updateDto, @MappingTarget VehiculoAsignacion asignacion);
}
