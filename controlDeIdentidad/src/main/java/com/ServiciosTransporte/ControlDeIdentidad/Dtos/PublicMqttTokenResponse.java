package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import java.io.Serializable;

public record PublicMqttTokenResponse(
        String accessToken,
        Long expiresIn
) implements Serializable {}
