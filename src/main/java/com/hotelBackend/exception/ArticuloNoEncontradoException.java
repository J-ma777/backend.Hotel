package com.hotelBackend.exception;

public class ArticuloNoEncontradoException extends RuntimeException {
    public ArticuloNoEncontradoException(Long articuloId) {

        super("No se encontró el artículo de inventario con id: " + articuloId);
    }
}
