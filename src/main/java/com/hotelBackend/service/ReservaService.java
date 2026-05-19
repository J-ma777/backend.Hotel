package com.hotelBackend.service;

import com.hotelBackend.controller.dto.CrearReservaRequest;
import com.hotelBackend.model.Reserva;

import java.util.List;

public interface ReservaService {

    Reserva crear(CrearReservaRequest request, Long userId);

    /**
     * Confirma una reserva PENDIENTE.
     * Flujo: PENDIENTE -> CONFIRMADA
     */
    Reserva confirmar(Long id);

    List<Reserva> listar();

    Reserva obtenerPorId(Long id);

    Reserva cancelar(Long id);

    Reserva marcarEnCasa(Long id);

    Reserva realizarCheckout(Long id);

    void procesarNoPresentadas();
}
