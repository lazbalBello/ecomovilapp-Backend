package com.EcoTransporte.GestionSeguimiento.MappersResponse;

import com.EcoTransporte.GestionSeguimiento.DtoResponse.AsignacionLiteDto;
import com.EcoTransporte.GestionSeguimiento.Modelos.Conductor;
import com.EcoTransporte.GestionSeguimiento.Modelos.VehiculoAsignacion;
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
