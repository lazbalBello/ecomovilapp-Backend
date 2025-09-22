package com.serviciosTransporte_controlDeIdentidad.Dtos;

import jakarta.validation.constraints.NotBlank;

public record RefreshToken(@NotBlank(message = "Se requiere un token de refresco") String refreshToken) {
}
