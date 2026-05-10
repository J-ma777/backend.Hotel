package com.hotelBackend.service;

import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.TicketMantenimiento;

public interface TicketMantenimientoService {

    TicketMantenimiento crearDesdeLimpieza(
            Habitacion habitacion,
            String descripcion,
            Long usuarioId
    );

    // Metodo de dominio para resolver un ticket de mantenimiento, se encarga de cambiar el estado del ticket a resuelto y actualizar la habitacion a disponible
    TicketMantenimiento resolverTicket(Long ticketId, Long usuarioId);

    TicketMantenimiento marcarEnProceso(Long id);


}
