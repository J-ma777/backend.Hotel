package com.hotelBackend.service.Implementaciones;


import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.RegistroLimpieza;
import com.hotelBackend.model.TicketMantenimiento;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoTicket;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.RegistroLimpiezaRepository;
import com.hotelBackend.repository.TicketMantenimientoRepository;
import com.hotelBackend.security.util.AuthUtil;
import com.hotelBackend.service.TicketMantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
            registro.setEstadoNuevo(EstadoHabitacion.SUCIA); // CAMBIO CLAVE: Ya que de repente usaron materiales que contenian polvo, entonces tienen que proceder a limpiar
                                                             // habitación antes de volver a ponerla en servicio, por eso el estado nuevo es SUCIA y no DISPONIBLE
            registro.setNotas("Liberación automática por ticket resuelto");
            registro.setCambiadoEn(LocalDateTime.now());
            registro.setCambiadoPor(usuarioId);

            // CAMBIO CLAVE
            habitacion.setEstado(EstadoHabitacion.SUCIA);

            habitacionRepository.save(habitacion);
            registroLimpiezaRepository.save(registro);
        }

        return ticketMantenimientoRepository.save(ticket);
    }

    @Override
    public TicketMantenimiento marcarEnProceso(Long id) {

        TicketMantenimiento ticket = ticketMantenimientoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));

        // validar estado actual
        if (ticket.getEstado() != EstadoTicket.ABIERTO) {
            throw new RuntimeException("Solo tickets en estado ABIERTO pueden pasar a EN_PROCESO");
        }

        // cambiar estado del ticket
        ticket.setEstado(EstadoTicket.EN_PROCESO);

        //  AQUÍ ESTÁ LA CLAVE
        // obtener habitación
        Habitacion habitacion = ticket.getHabitacion();

        // cambiar habitación a fuera de servicio
        habitacion.setEstado(EstadoHabitacion.FUERA_DE_SERVICIO);

        // guardar habitación
        habitacionRepository.save(habitacion);

        return ticketMantenimientoRepository.save(ticket);
    }

    @Override
    public TicketMantenimiento crearManual(Long habitacionId, String descripcion) {

        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        if (habitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new IllegalStateException("La habitación ya está fuera de servicio");
        }
        boolean existeTicketActivo = ticketMantenimientoRepository.existsByHabitacionIdAndEstadoIn(
                habitacionId,
                List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO)
        );
        if (existeTicketActivo) {
            throw new IllegalStateException("La habitación ya tiene un ticket activo");
        }

        TicketMantenimiento ticket = new TicketMantenimiento();

        ticket.setHabitacion(habitacion);
        ticket.setDescripcion(descripcion);
        ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setReportadoEn(LocalDateTime.now());
        ticket.setReportadoPor(AuthUtil.getCurrentUserId());

        // RF-12
        habitacion.setEstado(EstadoHabitacion.FUERA_DE_SERVICIO);

        // Guardar habitación
        habitacionRepository.save(habitacion);

        // Gusrdar ticket
        return ticketMantenimientoRepository.save(ticket);
    }

    @Override
    public List<TicketMantenimiento> listarTickets() {
        return ticketMantenimientoRepository.findAll(
                Sort.by(Sort.Direction.DESC, "reportadoEn")
        );
    }

}
