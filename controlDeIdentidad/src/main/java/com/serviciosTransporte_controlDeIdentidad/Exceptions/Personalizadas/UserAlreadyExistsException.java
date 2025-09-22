package com.serviciosTransporte_controlDeIdentidad.Exceptions.Personalizadas;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
