package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record RutaDto(
        /** Poblado al retornar la entidad guardada; ignorado al mapear DTO→Entidad. */
        Long id,

        @NotBlank(message = "El nombre de la ruta no debe estar en blanco")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
                message = "El nombre no debe tener caracteres especiales")
        String nombre,

        @NotNull(message = "Se debe seleccionar un recorrido")
        @NotEmpty(message = "Se debe seleccionar un recorrido")
        List<RecorridoRutaDto> recorrido,

        @Pattern(regexp = "^[\\p{L}\\p{N}\\s]+$",
                message = "La descripción no debe tener caracteres especiales")
        String descripcion
) {
}
