package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(

        @NotBlank(message = "El email no puede estar en blanco")
        String email,

        @NotBlank(message = "La contraseña  no puede estar en blanco")
        String password
) {
}
