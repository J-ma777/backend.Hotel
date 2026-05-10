package com.hotelBackend.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String nombreArticulo, double stockActual, int solicitado) {
        super(
                "Stock insuficiente para el artículo '" + nombreArticulo +
                        "'. Stock actual: " + stockActual +
                        ", solicitado: " + solicitado
        );
    }

}
