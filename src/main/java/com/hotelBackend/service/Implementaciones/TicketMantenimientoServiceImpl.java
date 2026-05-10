package com.hotelBackend.service.Implementaciones;


import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.RegistroLimpieza;
import com.hotelBackend.model.TicketMantenimiento;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoTicket;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.RegistroLimpiezaRepository;
import com.hotelBackend.repository.TicketMantenimientoRepository;
import com.hotelBackend.service.TicketMantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketMantenimientoServiceImpl implements TicketMantenimientoService {

    private final TicketMantenimientoRepository ticketMantenimientoRepository;
    private final HabitacionRepository habitacionRepository;
    private final RegistroLimpiezaRepository registroLimpiezaRepository;


    @Override
    public TicketMantenimiento crearDesdeLimpieza(
            Habitacion habitacion,
            String descripcion,
            Long usuarioId
    ) {
        TicketMantenimiento ticket = new TicketMantenimiento();

        ticket.setHabitacion(habitacion);
        ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setDescripcion(descripcion);
        ticket.setReportadoPor(usuarioId);
        ticket.setReportadoEn(LocalDateTime.now());

        return ticketMantenimientoRepository.save(ticket);
    }


    @Override
    public TicketMantenimiento resolverTicket(Long ticketId, Long usuarioId) {

        TicketMantenimiento ticket = ticketMantenimientoRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        if (ticket.getEstado() == EstadoTicket.RESUELTO) {
            throw new IllegalStateException("El ticket ya está resuelto");
        }

        ticket.setEstado(EstadoTicket.RESUELTO);
        ticket.setResueltoEn(LocalDateTime.now());

        Habitacion habitacion = ticket.getHabitacion();

        // Solo liberamos si estaba fuera de servicio
        if (habitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {

            RegistroLimpieza registro = new RegistroLimpieza();
            registro.setHabitacion(habitacion);
            registro.setEstadoAnterior(EstadoHabitacion.FUERA_DE_SERVICIO);
            registro.setEstadoNuevo(EstadoHabitacion.DISPONIBLE);
            registro.setNotas("Liberación automática por ticket resuelto");
            registro.setCambiadoEn(LocalDateTime.now());
            registro.setCambiadoPor(usuarioId);

            habitacion.setEstado(EstadoHabitacion.DISPONIBLE);

            habitacionRepository.save(habitacion);
            registroLimpiezaRepository.save(registro);
        }

        return ticketMantenimientoRepository.save(ticket);
    }

    @Override
    public TicketMantenimiento marcarEnProceso(Long id) {

        TicketMantenimiento ticket = ticketMantenimientoRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException("Ticket de mantenimiento no encontrado"));

        if (ticket.getEstado() != EstadoTicket.ABIERTO) {
            throw new IllegalStateException(
                    "Solo un ticket ABIERTO puede pasar a EN_PROCESO"
            );
        }

        ticket.setEstado(EstadoTicket.EN_PROCESO);
        return ticketMantenimientoRepository.save(ticket);
    }
}
