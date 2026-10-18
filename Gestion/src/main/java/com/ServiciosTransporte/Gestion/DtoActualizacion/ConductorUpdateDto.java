package com.ServiciosTransporte.Gestion.DtoActualizacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ConductorUpdateDto(
        @Pattern(
                regexp = "^\\d{11}$",
                message = "El dni deben ser 11 dígitos númericos sin espacios"
        )
        String dni,

        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
                message = "El nombre solo puede contener letras y espacios"
        )
        String nombre,

        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
                message = "Los apellidos solo pueden contener letras y espacios"
        )
        String apellidos,

        List<@NotBlank(message = "La categoria no puede estar vacía")
                @Pattern(regexp = "^(?:FE|[ACD]1?|[BEF])$",
                        message = "Categoria no válida")
                String> categoriasLicencia,

        Boolean disponibilidad
) {
    public ConductorUpdateDto {
        if (disponibilidad == null) {
            disponibilidad = true;
        }
    }
}
