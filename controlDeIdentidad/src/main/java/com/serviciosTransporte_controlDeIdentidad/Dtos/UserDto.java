package com.serviciosTransporte_controlDeIdentidad.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


public record UserDto(

        @NotBlank(message = "El nombre de usuario no puede estar en blanco")
        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
                message = "El nombre solo puede contener letras y espacios"
        )
        String nombreUsuario,

        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
                message = "El nombre solo puede contener letras y espacios"
        )
        String nombre,

        @Pattern(
                regexp = "^[A-Za-zÁÉÍÓÚáéíóúñÑ\\s]+$",
                message = "Los qpellidos solo pueden contener letras y espacios"
        )
        String apellido,

        @NotBlank(message = "El email no puede estar en blanco")
        @Email(message = "El formato del email no es válido")
        String email,

        @NotBlank(message = "La contraseña  no puede estar en blanco")
        String password
) {
}
