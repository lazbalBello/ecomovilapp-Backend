package com.ServiciosTransporte.Gestion.DtoRespuesta.Asignaciones;

import java.time.LocalDate;

public record AsignacionLiteDto(
        Long id,
        String matriculaVehiculo,
        String conductor,
        String dniConductor,
        LocalDate fechaInicio,
        LocalDate fechaFinal,
        boolean indefinido
) {
}
