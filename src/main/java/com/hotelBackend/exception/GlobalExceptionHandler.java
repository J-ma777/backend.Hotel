package com.hotelBackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReservaNoEncontradaException.class)
    public void handleReservaNoEncontrada() {}

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ReservaNoEnCasaException.class)
    public void handleReservaNoEnCasa() {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ArticuloNoEncontradoException.class)
    public void handleArticuloNoEncontrado() {}

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(StockInsuficienteException.class)
    public void handleStockInsuficiente() {}
}