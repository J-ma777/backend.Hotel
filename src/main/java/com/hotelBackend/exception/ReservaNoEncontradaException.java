package com.hotelBackend.exception;

public class ReservaNoEncontradaException extends RuntimeException {
    public ReservaNoEncontradaException(Long reservaId) {
        super("No se encontró la reserva con id: " + reservaId);
    }
    // Ejemplo de dominio puro de la capa de negocio, sin ninguna referencia a la capa de infraestructura o a detalles de implementación.
}
