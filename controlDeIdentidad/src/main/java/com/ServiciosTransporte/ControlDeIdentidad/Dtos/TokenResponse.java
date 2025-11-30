package com.ServiciosTransporte.ControlDeIdentidad.Dtos;

import java.io.Serializable;

public record TokenResponse(String jwtToken, String refreshToken, Long expireIn, Long refreshExpiresIn) implements Serializable {}
