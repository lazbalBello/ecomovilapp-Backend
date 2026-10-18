package com.ServiciosTransporte.Gestion.DtoActualizacion;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record AsignacionUpdateDto(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaInicio,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaFinal,

        boolean indefinido
) {
}
