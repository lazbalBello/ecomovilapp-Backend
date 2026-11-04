package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RecuperarPasswordDto(
        @Email(message = "El formato del email es inválido")
        @NotBlank(message = "El email no puede estar vacío")
        String email
) {}
