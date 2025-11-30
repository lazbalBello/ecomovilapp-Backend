package com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas;

public class UserCreationException extends RuntimeException{
    public UserCreationException(String message) {
        super(message);
    }
}

