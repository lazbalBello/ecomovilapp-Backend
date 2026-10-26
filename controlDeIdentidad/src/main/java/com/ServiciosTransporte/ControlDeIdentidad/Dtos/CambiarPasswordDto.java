package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordDto(
        @NotBlank(message = "La contraseña actual es requerida")
        String passwordActual,

        @NotBlank(message = "La nueva contraseña es requerida")
        @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
        String passwordNuevo
) {}
