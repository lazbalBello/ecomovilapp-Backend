package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(

        @JsonAlias({"username", "email"})
        @NotBlank(message = "El usuario o email no puede estar en blanco")
        String email,

        @NotBlank(message = "La contraseña no puede estar en blanco")
        String password
) {
}
