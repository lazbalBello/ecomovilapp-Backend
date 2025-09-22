package com.serviciosTransporte_controlDeIdentidad.Exceptions.Personalizadas;

public class UserCreationException extends RuntimeException{
    public UserCreationException(String message) {
        super(message);
    }
}

