package com.ServiciosTransporte.Gestion.DtoActualizacion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record VehiculoUpdateDto(
        @Pattern(regexp = "^[A-Za-z]\\d{6}$",
                message = "Formato de matrícula no válido")
        String matricula,

        String gpsId,

        @Min(value = 1, message = "La capacidad de personas debe ser al menos 1")
        Integer capacidadPersonas,

        String modelo,

        String marca,

        String tipoBateria,

        Integer estado,

        Long rutaId
) {
}
