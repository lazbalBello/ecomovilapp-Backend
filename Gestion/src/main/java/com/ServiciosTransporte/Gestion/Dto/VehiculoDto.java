package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VehiculoDto(
        /** Poblado al retornar la entidad guardada; ignorado al mapear DTO→Entidad. */
        Long id,

        @NotBlank(message = "Se requiere la matrícula")
        @Pattern(regexp = "^[A-Za-z]\\d{6}$",
                message = "Formato de matrícula no válido")
        String matricula,

        String gpsId,

        @NotNull(message = "Se requiere la capacidad de personas")
        @Min(value = 1, message = "La capacidad de personas debe ser al menos 1")
        Integer capacidadPersonas,

        @NotBlank(message = "Se requiere el modelo")
        String modelo,

        @NotBlank(message = "Se requiere la marca")
        String marca,

        @NotBlank(message = "Se requiere el tipo de bateria")
        String tipoBateria,

        String estado
) {
    public VehiculoDto {
        if (estado == null) {
            estado = "ACTIVO";
        }
    }
}
