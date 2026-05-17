package com.hotelBackend.exception;

import com.hotelBackend.controller.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 404 - No encontrado
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ReservaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleReservaNoEncontrada(ReservaNoEncontradaException ex) {
        log.warn("Reserva no encontrada: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Reserva no encontrada", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ArticuloNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleArticuloNoEncontrado(ArticuloNoEncontradoException ex) {
        log.warn("Artículo no encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Artículo no encontrado", ex.getMessage()));
    }

    // 409 - Conflicto (estado inválido, recurso no disponible)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ReservaNoEnCasaException.class)
    public ResponseEntity<ErrorResponse> handleReservaNoEnCasa(ReservaNoEnCasaException ex) {
        log.warn("Estado de reserva inválido: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Operación no permitida en estado actual", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EstadoReservaInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleEstadoReservaInvalido(EstadoReservaInvalidoException ex) {
        log.warn("Transición de estado inválida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Transición de estado no permitida", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(HabitacionNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleHabitacionNoDisponible(HabitacionNoDisponibleException ex) {
        log.warn("Habitación no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Habitación no disponible", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        log.warn("Conflicto de estado/regla de negocio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(409, "Conflicto de estado", ex.getMessage()));
    }

    // 400 - Bad Request (validaciones)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidacionFechasException.class)
    public ResponseEntity<ErrorResponse> handleValidacionFechas(ValidacionFechasException ex) {
        log.warn("Validación de fechas fallida: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Validación de fechas inválida", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, "Solicitud inválida", ex.getMessage()));
    }

    // 422 - Unprocessable Entity (stock insuficiente)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuficiente(StockInsuficienteException ex) {
        log.warn("Stock insuficiente: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(422, "Stock insuficiente", ex.getMessage()));
    }

    // 500 - Error interno genérico
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Error interno no manejado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Error interno del servidor", ex.getMessage()));
    }
}