package com.hotelbackend.service;

import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.RegistroLimpieza;
import com.hotelbackend.model.enums.EstadoHabitacion;
import com.hotelbackend.model.enums.EstadoTicket;
import com.hotelbackend.repository.HabitacionRepository;
import com.hotelbackend.repository.RegistroLimpiezaRepository;
import com.hotelbackend.repository.TicketMantenimientoRepository;
import com.hotelbackend.repository.ReservaRepository;
import com.hotelbackend.service.Implementaciones.RegistroLimpiezaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroLimpiezaServiceImplTest {

    @Mock
    private RegistroLimpiezaRepository registroLimpiezaRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private TicketMantenimientoService ticketMantenimientoService;

    @Mock
    private TicketMantenimientoRepository ticketMantenimientoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private RegistroLimpiezaServiceImpl service;

    private Habitacion habitacion;

    @BeforeEach
    void setup() {
        habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setEstado(EstadoHabitacion.LIMPIANDO);
    }

    @Test
    void inspeccionada_sinIncidencias_debePasarADisponible() {

        when(habitacionRepository.findById(1L))
                .thenReturn(Optional.of(habitacion));

        when(reservaRepository.existsByHabitacionIdAndEstadoIn(eq(1L), anyList()))
                .thenReturn(false);

        when(ticketMantenimientoRepository
                .existsByHabitacionIdAndEstado(1L, EstadoTicket.ABIERTO))
                .thenReturn(false);

        when(registroLimpiezaRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        RegistroLimpieza registro = service.registrarCambioEstado(
                1L,
                EstadoHabitacion.INSPECCIONADA,
                null,
                10L
        );

        // Estado final
        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());

        // Registro
        assertNotNull(registro);
        assertEquals(EstadoHabitacion.LIMPIANDO, registro.getEstadoAnterior());
        assertEquals(EstadoHabitacion.INSPECCIONADA, registro.getEstadoNuevo());
        assertEquals(habitacion, registro.getHabitacion());

        // Persistencia
        verify(habitacionRepository).save(habitacion);
        verify(registroLimpiezaRepository).save(any());

        // Sin ticket
        verify(ticketMantenimientoService, never())
                .crearDesdeLimpieza(any(), any(), any());
    }

    @Test
    void inspeccionada_conNotas_debeCrearTicket_yMantenerEstado() {

        when(habitacionRepository.findById(1L))
                .thenReturn(Optional.of(habitacion));

        when(reservaRepository.existsByHabitacionIdAndEstadoIn(eq(1L), anyList()))
                .thenReturn(false);

        when(ticketMantenimientoRepository
                .existsByHabitacionIdAndEstado(1L, EstadoTicket.ABIERTO))
                .thenReturn(true);

        when(registroLimpiezaRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.registrarCambioEstado(
                1L,
                EstadoHabitacion.INSPECCIONADA,
                "fuga en lavamanos",
                10L
        );

        // No pasa a disponible
        assertEquals(EstadoHabitacion.INSPECCIONADA, habitacion.getEstado());

        // Se crea ticket
        verify(ticketMantenimientoService).crearDesdeLimpieza(
                habitacion,
                "fuga en lavamanos",
                10L
        );

        verify(habitacionRepository).save(habitacion);
        verify(registroLimpiezaRepository).save(any());
    }

    @Test
    void fueraDeServicio_debeActualizarEstado_yCrearTicket() {

        when(habitacionRepository.findById(1L))
                .thenReturn(Optional.of(habitacion));

        when(reservaRepository.existsByHabitacionIdAndEstadoIn(eq(1L), anyList()))
                .thenReturn(false);

        when(registroLimpiezaRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        service.registrarCambioEstado(
                1L,
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

        verify(habitacionRepository).save(habitacion);
        verify(registroLimpiezaRepository).save(any());
    }

    @Test
    void mismoEstado_debeLanzarExcepcion() {

        habitacion.setEstado(EstadoHabitacion.SUCIA);

        when(habitacionRepository.findById(1L))
                .thenReturn(Optional.of(habitacion));

        when(reservaRepository.existsByHabitacionIdAndEstadoIn(eq(1L), anyList()))
                .thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                service.registrarCambioEstado(
                        1L,
                        EstadoHabitacion.SUCIA,
                        null,
                        1L
                )
        );

        verifyNoInteractions(registroLimpiezaRepository);
        verify(habitacionRepository, never()).save(any());
    }
}
