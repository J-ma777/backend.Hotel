package com.hotelBackend.exception;

public class ReservaNoEnCasaException extends RuntimeException {

    public ReservaNoEnCasaException(Long reservaId) {
        super("La reserva con id" + reservaId + " no se encuentra EN_CASA.");
    }
}
