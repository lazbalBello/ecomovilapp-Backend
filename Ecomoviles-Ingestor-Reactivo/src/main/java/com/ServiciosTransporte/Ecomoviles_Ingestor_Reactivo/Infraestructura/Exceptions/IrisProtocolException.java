package com.ServiciosTransporte.Ecomoviles_Ingestor_Reactivo.Infraestructura.Exceptions;

/**
 * Excepción para fallos de formato, parseo o límites lógicos en el protocolo IRIS.
 */
public class IrisProtocolException extends RuntimeException {

    public IrisProtocolException(String message) {
        super(message);
    }

    public IrisProtocolException(String message, Throwable cause) {
        super(message, cause);
    }
}
