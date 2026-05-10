package com.hotelBackend.service;

import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.RegistroLimpieza;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoTicket;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.RegistroLimpiezaRepository;
import com.hotelBackend.repository.TicketMantenimientoRepository;
import com.hotelBackend.service.Implementaciones.RegistroLimpiezaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegistroLimpiezaServiceImplTest {

    @Mock
    private RegistroLimpiezaRepository registroLimpiezaRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private TicketMantenimientoService ticketMantenimientoService;

    @Mock
    private TicketMantenimientoRepository ticketMantenimientoRepository;

    @InjectMocks
    private RegistroLimpiezaServiceImpl registroLimpiezaService;

    @Test
    void inspeccionada_sin_incidencias_pasa_a_disponible() {
        Habitacion habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setEstado(EstadoHabitacion.LIMPIANDO);

        when(habitacionRepository.findById(1L))
                .thenReturn(Optional.of(habitacion));

        when(ticketMantenimientoRepository
                .existsByHabitacionIdAndEstado(1L, EstadoTicket.ABIERTO))
                .thenReturn(false);

        when(registroLimpiezaRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegistroLimpieza registro = registroLimpiezaService.registrarCambioEstado(
                1L,
                EstadoHabitacion.INSPECCIONADA,
                null,
                10L
        );

        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());
        assertEquals(EstadoHabitacion.LIMPIANDO, registro.getEstadoAnterior());
        assertEquals(EstadoHabitacion.INSPECCIONADA, registro.getEstadoNuevo());

        verify(ticketMantenimientoService, never())
                .crearDesdeLimpieza(any(), any(), any());
    }

    @Test
    void inspeccionada_con_notas_crea_ticket_y_no_pasa_a_disponible() {
        Habitacion habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setEstado(EstadoHabitacion.LIMPIANDO);

        when(habitacionRepository.findById(1L))
                .thenReturn(Optional.of(habitacion));

        when(ticketMantenimientoRepository
                .existsByHabitacionIdAndEstado(1L, EstadoTicket.ABIERTO))
                .thenReturn(true);

        when(registroLimpiezaRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registroLimpiezaService.registrarCambioEstado(
                1L,
                EstadoHabitacion.INSPECCIONADA,
                "fuga en lavamanos",
                10L
        );

        assertEquals(EstadoHabitacion.INSPECCIONADA, habitacion.getEstado());

        verify(ticketMantenimientoService).crearDesdeLimpieza(
                habitacion,
                "fuga en lavamanos",
                10L
        );
    }

    @Test
    void fuera_de_servicio_genera_ticket() {
        Habitacion habitacion = new Habitacion();
        habitacion.setId(2L);
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);

        when(habitacionRepository.findById(2L))
                .thenReturn(Optional.of(habitacion));

        when(registroLimpiezaRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        registroLimpiezaService.registrarCambioEstado(
                2L,
                EstadoHabitacion.FUERA_DE_SERVICIO,
                "aire acondicionado dañado",
                20L
        );

        assertEquals(EstadoHabitacion.FUERA_DE_SERVICIO, habitacion.getEstado());

        verify(ticketMantenimientoService).crearDesdeLimpieza(
                habitacion,
                "aire acondicionado dañado",
                20L
        );
    }

    @Test
    void mismo_estado_lanza_excepcion() {
        Habitacion habitacion = new Habitacion();
        habitacion.setId(3L);
        habitacion.setEstado(EstadoHabitacion.SUCIA);

        when(habitacionRepository.findById(3L))
                .thenReturn(Optional.of(habitacion));

        assertThrows(IllegalStateException.class, () ->
                registroLimpiezaService.registrarCambioEstado(
                        3L,
                        EstadoHabitacion.SUCIA,
                        null,
                        1L
                )
        );

        verifyNoInteractions(registroLimpiezaRepository);
    }
}
