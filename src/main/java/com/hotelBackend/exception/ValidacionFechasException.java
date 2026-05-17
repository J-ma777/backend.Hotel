package com.hotelBackend.exception;

public class ValidacionFechasException extends RuntimeException {

    public ValidacionFechasException(String message) {
        super(message);
    }

    public ValidacionFechasException(String message, Throwable cause) {
        super(message, cause);
    }
}

