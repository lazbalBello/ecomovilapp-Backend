package com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
