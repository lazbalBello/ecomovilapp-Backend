package com.ServiciosTransporte.Gestion.Dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record VehiculoAsignacionDto(
        @NotNull
        Long vehiculoId,

        @NotNull
        Long conductorId,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @NotNull(message = "Se requiere fecha inicio")
        LocalDate fechaInicio,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaFinal,

        @NotNull(message = "Se debe indicar si el tiempo es indefinido")
        boolean indefinido
) {
}
