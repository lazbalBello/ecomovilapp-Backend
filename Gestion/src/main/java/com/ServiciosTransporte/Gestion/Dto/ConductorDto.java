package com.ServiciosTransporte.Gestion.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ConductorDto(
        /** Poblado al retornar la entidad guardada; ignorado al mapear DTO→Entidad. */
        Long id,

        @NotBlank(message = "El dni no puede estar en blanco")
        @Pattern(
                regexp = "^\\d{11}$",
                message = "El dni deben ser 11 dígitos númericos sin espacios"
        )
        String dni,

        @NotBlank
        String usuarioId,

        @NotBlank(message = "El nombre no puede estar en blanco")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
                message = "El nombre solo puede contener letras y espacios"
        )
        String nombre,

        @NotBlank(message = "Los apellidos no pueden estar en blanco")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
                message = "Los apellidos solo pueden contener letras y espacios"
        )
        String apellidos,

        @NotEmpty(message = "El conducor debe tener al menos una categoria de licencia")
        List<@NotBlank(message = "La categoria no puede estar vacía")
                @Pattern(regexp = "^(?:FE|[ACD]1?|[BEF])$",
                        message = "Categoria no válida")
                String> categoriasLicencia,

        Boolean disponibilidad
) {
    public ConductorDto {
        if (disponibilidad == null) {
            disponibilidad = true;
        }
    }
}
