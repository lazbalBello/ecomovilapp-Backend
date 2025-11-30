package com.ServiciosTransporte.Gestion.MappersResponse;

import com.ServiciosTransporte.Gestion.DtoResponse.AsignacionLiteDto;
import com.ServiciosTransporte.Gestion.Modelos.Conductor;
import com.ServiciosTransporte.Gestion.Modelos.VehiculoAsignacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AsignacionLiteDtoMapper {

    @Mapping(source = "conductor.dni", target = "dniConductor")
    @Mapping(source = "vehiculo.matricula", target = "matriculaVehiculo")
    @Mapping(target = "conductor", expression = "java(nombreConductor(vehiculoAsignacion.getConductor()))")
    AsignacionLiteDto toAsignacionLiteDto(VehiculoAsignacion vehiculoAsignacion);

    default String nombreConductor(Conductor conductor){
        return (conductor != null) ? conductor.getNombre() + " " + conductor.getApellidos()
                : null;
    }
}
