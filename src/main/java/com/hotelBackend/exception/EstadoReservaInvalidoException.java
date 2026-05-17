package com.hotelBackend.exception;

public class EstadoReservaInvalidoException extends RuntimeException {

    public EstadoReservaInvalidoException(String message) {
        super(message);
    }

    public EstadoReservaInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}
