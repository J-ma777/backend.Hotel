package com.hotelBackend.service;

import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.PlanTarifario;
import com.hotelBackend.model.Reserva;
import com.hotelBackend.model.TipoHabitacion;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoReserva;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.ReservaRepository;
import com.hotelBackend.service.Implementaciones.ReservaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private TransaccionFolioService transaccionFolioService;

    @Mock
    private PlanTarifarioService planTarifarioService;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setFechaEntrada(LocalDate.now().plusDays(1));
        reserva.setFechaSalida(LocalDate.now().plusDays(3));
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        Habitacion habitacion = new Habitacion();
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);

        TipoHabitacion tipoHabitacion = new TipoHabitacion();
        tipoHabitacion.setId(1L);
        tipoHabitacion.setNombre("Habitación Estándar");

        habitacion.setTipoHabitacion(tipoHabitacion);
        reserva.setHabitacion(habitacion);
    }

    // CREAR RESERVA

    @Test
    void crear_reserva_valida_ok() {
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        Reserva resultado = reservaService.crear(reserva);

        assertNotNull(resultado);
        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void crear_reserva_fechas_invalidas_lanza_excepcion() {
        reserva.setFechaSalida(reserva.getFechaEntrada());

        assertThrows(IllegalArgumentException.class,
                () -> reservaService.crear(reserva));

        verify(reservaRepository, never()).save(any());
    }

    // OBTENER

    @Test
    void obtenerPorId_no_existente_lanza_excepcion() {
        when(reservaRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reservaService.obtenerPorId(1L));
    }

    // CANCELAR

    @Test
    void cancelar_reserva_confirmada_ok() {
        Habitacion habitacion = reserva.getHabitacion();
        habitacion.setEstado(EstadoHabitacion.OCUPADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));
        when(habitacionRepository.save(any()))
                .thenReturn(habitacion);
        when(reservaRepository.save(any()))
                .thenReturn(reserva);

        Reserva resultado = reservaService.cancelar(1L);

        assertEquals(EstadoReserva.CANCELADA, resultado.getEstado());
        assertEquals(EstadoHabitacion.DISPONIBLE, habitacion.getEstado());
    }

    @Test
    void cancelar_reserva_checkout_lanza_excepcion() {
        reserva.setEstado(EstadoReserva.SALIDA_CHECKOUT);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(IllegalStateException.class,
                () -> reservaService.cancelar(1L));
    }

    // CHECK-IN (MARCAR EN CASA)

    @Test
    void marcar_en_casa_desde_confirmada_ok() {

        PlanTarifario tarifa = new PlanTarifario();
        tarifa.setNombre("Tarifa Test");
        tarifa.setPrecioPorNoche(BigDecimal.valueOf(100));

        when(planTarifarioService.obtenerTarifaParaNoche(anyLong(), any()))
                .thenReturn(tarifa);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));
        when(habitacionRepository.save(any()))
                .thenReturn(reserva.getHabitacion());
        when(reservaRepository.save(any()))
                .thenReturn(reserva);

        Reserva resultado = reservaService.marcarEnCasa(1L);

        assertEquals(EstadoReserva.EN_CASA, resultado.getEstado());
        assertEquals(EstadoHabitacion.OCUPADA,
                reserva.getHabitacion().getEstado());
    }

    // CHECKOUT

    @Test
    void checkout_desde_en_casa_ok() {

        reserva.setEstado(EstadoReserva.EN_CASA);
        reserva.getHabitacion().setEstado(EstadoHabitacion.OCUPADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));
        when(habitacionRepository.save(any()))
                .thenReturn(reserva.getHabitacion());
        when(reservaRepository.save(any()))
                .thenReturn(reserva);

        Reserva resultado = reservaService.realizarCheckout(1L);

        assertEquals(EstadoReserva.SALIDA_CHECKOUT, resultado.getEstado());
        assertEquals(EstadoHabitacion.DISPONIBLE,
                reserva.getHabitacion().getEstado());
    }

    @Test
    void checkout_desde_estado_invalido_lanza_excepcion() {
        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(IllegalStateException.class,
                () -> reservaService.realizarCheckout(1L));
    }
}