package com.hotelBackend.service;

import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.RegistroLimpieza;
import com.hotelBackend.model.TicketMantenimiento;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoTicket;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.RegistroLimpiezaRepository;
import com.hotelBackend.repository.TicketMantenimientoRepository;
import com.hotelBackend.service.Implementaciones.TicketMantenimientoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TicketMantenimientoServiceImplTest {

    @Mock
    private TicketMantenimientoRepository ticketMantenimientoRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private RegistroLimpiezaRepository registroLimpiezaRepository;

    @InjectMocks
    private TicketMantenimientoServiceImpl ticketMantenimientoService;

    @Test
    void resolver_ticket_libera_habitacion_fuera_de_servicio_y_registra_limpieza() {

        Habitacion habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setEstado(EstadoHabitacion.FUERA_DE_SERVICIO);

        TicketMantenimiento ticket = new TicketMantenimiento();
        ticket.setId(100L);
        ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setHabitacion(habitacion);

        when(ticketMantenimientoRepository.findById(100L))
                .thenReturn(Optional.of(ticket));

        when(ticketMantenimientoRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        TicketMantenimiento resultado =
                ticketMantenimientoService.resolverTicket(100L, 10L);

        assertEquals(EstadoTicket.RESUELTO, resultado.getEstado());
        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());

        verify(registroLimpiezaRepository).save(any(RegistroLimpieza.class));
        verify(habitacionRepository).save(habitacion);
    }

    @Test
    void resolver_ticket_no_libera_habitacion_si_no_esta_fuera_de_servicio() {

        Habitacion habitacion = new Habitacion();
        habitacion.setId(2L);
        habitacion.setEstado(EstadoHabitacion.INSPECCIONADA);

        TicketMantenimiento ticket = new TicketMantenimiento();
        ticket.setId(101L);
        ticket.setEstado(EstadoTicket.ABIERTO);
        ticket.setHabitacion(habitacion);

        when(ticketMantenimientoRepository.findById(101L))
                .thenReturn(Optional.of(ticket));

        when(ticketMantenimientoRepository.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        ticketMantenimientoService.resolverTicket(101L, 20L);

        assertEquals(EstadoHabitacion.INSPECCIONADA, habitacion.getEstado());

        verify(registroLimpiezaRepository, never()).save(any());
        verify(habitacionRepository, never()).save(any());
    }

    @Test
    void resolver_ticket_ya_resuelto_lanza_excepcion() {

        TicketMantenimiento ticket = new TicketMantenimiento();
        ticket.setId(102L);
        ticket.setEstado(EstadoTicket.RESUELTO);

        when(ticketMantenimientoRepository.findById(102L))
                .thenReturn(Optional.of(ticket));

        assertThrows(
                IllegalStateException.class,
                () -> ticketMantenimientoService.resolverTicket(102L, 30L)
        );

        verify(ticketMantenimientoRepository, never()).save(any());
    }


}
