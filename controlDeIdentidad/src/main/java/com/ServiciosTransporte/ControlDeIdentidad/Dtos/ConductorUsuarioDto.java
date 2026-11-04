package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record ConductorUsuarioDto(
        @NotBlank(message = "El nombre de usuario no puede estar en blanco")
        String nombreUsuario,

        @NotBlank(message = "La contraseña no puede estar en blanco")
        String password,

        @NotBlank(message = "El email no puede estar en blanco")
        @Email
        String email,

        @NotBlank(message = "El dni no puede estar en blanco")
        @Pattern(
                regexp = "^\\d{11}$",
                message = "El dni deben ser 11 dígitos númericos sin espacios"
        )
        String dni,

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
    public ConductorUsuarioDto {
        if (disponibilidad == null) {
            disponibilidad = false;
        }
    }
}
