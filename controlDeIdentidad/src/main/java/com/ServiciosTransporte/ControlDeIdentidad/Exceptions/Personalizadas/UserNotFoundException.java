package com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
