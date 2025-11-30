package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import jakarta.validation.constraints.NotBlank;

public record RefreshToken(@NotBlank(message = "Se requiere un token de refresco") String refreshToken) {
}
