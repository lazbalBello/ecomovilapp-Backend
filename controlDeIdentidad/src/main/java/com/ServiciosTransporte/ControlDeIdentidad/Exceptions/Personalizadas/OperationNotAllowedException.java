package com.ServiciosTransporte.ControlDeIdentidad.Exceptions.Personalizadas;

public class OperationNotAllowedException extends RuntimeException {
    public OperationNotAllowedException(String message) {
        super(message);
    }
}
