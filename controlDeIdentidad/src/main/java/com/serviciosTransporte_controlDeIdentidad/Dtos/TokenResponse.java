package com.serviciosTransporte_controlDeIdentidad.Dtos;

import java.io.Serializable;

public record TokenResponse(String jwtToken, String refreshToken, Long expireIn, Long refreshExpiresIn) implements Serializable {}
