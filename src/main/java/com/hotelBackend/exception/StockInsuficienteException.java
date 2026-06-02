package com.hotelBackend.exception;

import java.math.BigDecimal;

public class StockInsuficienteException extends RuntimeException {


    public StockInsuficienteException(
            String nombreArticulo,
            BigDecimal stockActual,
            BigDecimal solicitado
    ) {
        super("Stock insuficiente para el artículo " + nombreArticulo +
                ". Disponible: " + stockActual +
                ", solicitado: " + solicitado);
    }
}
