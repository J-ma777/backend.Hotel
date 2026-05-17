package com.hotelBackend.exception;

public class HabitacionNoDisponibleException extends RuntimeException {

    public HabitacionNoDisponibleException(String message) {
        super(message);
    }

    public HabitacionNoDisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}

