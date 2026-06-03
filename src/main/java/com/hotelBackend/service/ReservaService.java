package com.hotelBackend.service;

import com.hotelBackend.dto.request.CrearReservaRequest;
import com.hotelBackend.dto.response.ReservaResponse;
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

    ReservaResponse obtenerPorId(Long id);

    Reserva cancelar(Long id);

    Reserva marcarEnCasa(Long id);

    Reserva realizarcheckIn(Long reservaId, Long habitacionId);

    Reserva realizarCheckout(Long id);

    void procesarNoPresentadas();
}
