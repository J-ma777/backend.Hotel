package com.hotelbackend.service;

import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.TicketMantenimiento;

import java.util.List;

public interface TicketMantenimientoService {

    TicketMantenimiento crearDesdeLimpieza(
            Habitacion habitacion,
            String descripcion,
            Long usuarioId
    );

    // Metodo de dominio para resolver un ticket de mantenimiento, se encarga de cambiar el estado del ticket a resuelto y actualizar la habitacion a disponible
    TicketMantenimiento resolverTicket(Long ticketId, Long usuarioId);

    TicketMantenimiento marcarEnProceso(Long id);

    TicketMantenimiento crearManual(Long habitacionId, String descripcion);

    List<TicketMantenimiento> listarTickets();
}
