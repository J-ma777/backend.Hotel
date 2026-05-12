package com.hotelBackend.service;

import com.hotelBackend.controller.dto.CrearReservaRequest;
import com.hotelBackend.model.Reserva;

import java.math.BigDecimal;
import java.util.List;

public interface ReservaService {

    Reserva crear(CrearReservaRequest request);

    List<Reserva> listar();

    Reserva obtenerPorId(Long id);

    Reserva cancelar(Long id);

    Reserva marcarEnCasa(Long id);

    Reserva realizarCheckout(Long id);

    void procesarNoPresentadas();
}
