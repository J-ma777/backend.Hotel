package com.hotelbackend.service;

import com.hotelbackend.dto.response.FolioResumenResponse;
import com.hotelbackend.dto.response.ReservaResponse;
import com.hotelbackend.exception.EstadoReservaInvalidoException;
import com.hotelbackend.exception.HabitacionNoDisponibleException;
import com.hotelbackend.exception.ReservaNoEncontradaException;
import com.hotelbackend.exception.ValidacionFechasException;
import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.PlanTarifario;
import com.hotelbackend.model.Reserva;
import com.hotelbackend.model.TipoHabitacion;
import com.hotelbackend.model.enums.EstadoHabitacion;
import com.hotelbackend.model.enums.EstadoReserva;
import com.hotelbackend.repository.HabitacionRepository;
import com.hotelbackend.repository.PlanTarifarioRepository;
import com.hotelbackend.repository.ReservaRepository;
import com.hotelbackend.service.implementaciones.ReservaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.hotelbackend.dto.request.CrearReservaRequest;

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

    @Mock
    private PlanTarifarioRepository planTarifarioRepository;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    private Reserva reserva;
    private Habitacion habitacion;
    private TipoHabitacion tipoHabitacion;

    @BeforeEach
    void setUp() {
        tipoHabitacion = new TipoHabitacion();
        tipoHabitacion.setId(1L);
        tipoHabitacion.setNombre("Habitación Estándar");
        tipoHabitacion.setCapacidad(2);

        habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacion.setTipoHabitacion(tipoHabitacion);

        reserva = new Reserva();
        reserva.setId(1L);
        // Usar fechas que permitan check-in hoy
        reserva.setFechaEntrada(LocalDate.now());
        reserva.setFechaSalida(LocalDate.now().plusDays(3));
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setCantidadHuespedes(2);
        reserva.setHabitacion(habitacion);
    }

    // ========== CREAR RESERVA ==========

    @Test
    void crear_reserva_valida_ok() {
        CrearReservaRequest request = new CrearReservaRequest();
        request.setFechaEntrada(LocalDate.now().plusDays(1));
        request.setFechaSalida(LocalDate.now().plusDays(3));
        request.setCantidadHuespedes(2);
        request.setNombreHuesped("Juan Perez");
        request.setDocumentoHuesped("72345678");
        request.setTipoHabitacionId(1L);
        request.setPlanTarifarioId(1L);

        PlanTarifario plan = new PlanTarifario();
        plan.setId(1L);

        when(planTarifarioRepository.findById(anyLong())).thenReturn(Optional.of(plan));
        when(reservaRepository.save(any(Reserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Reserva resultado = reservaService.crear(request, 1L);

        assertNotNull(resultado);
        assertEquals(EstadoReserva.PENDIENTE, resultado.getEstado());
        assertEquals("Juan Perez", resultado.getNombreHuesped());
        assertEquals(1L, resultado.getCreadoPor());

        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void confirmar_reserva_pendiente_ok() {
        reserva.setEstado(EstadoReserva.PENDIENTE);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));
        when(reservaRepository.save(any()))
                .thenReturn(reserva);

        Reserva resultado = reservaService.confirmar(1L);

        assertEquals(EstadoReserva.CONFIRMADA, resultado.getEstado());
    }

    @Test
    void confirmar_reserva_no_pendiente_lanza_excepcion() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(EstadoReservaInvalidoException.class,
                () -> reservaService.confirmar(1L));
    }

    // ========== OBTENER ==========

    @Test
    void obtenerPorId_existe_ok() {
        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        ReservaResponse resultado = reservaService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    void obtenerPorId_no_existente_lanza_excepcion() {
        when(reservaRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ReservaNoEncontradaException.class,
                () -> reservaService.obtenerPorId(1L));
    }

    // ========== CANCELAR ==========

    @Test
    void cancelar_reserva_confirmada_ok() {
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

        assertThrows(EstadoReservaInvalidoException.class,
                () -> reservaService.cancelar(1L));
    }

    // ========== CHECK-IN (MARCAR EN CASA) ==========

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
                .thenReturn(habitacion);
        when(reservaRepository.save(any()))
                .thenReturn(reserva);

        Reserva resultado = reservaService.marcarEnCasa(1L);

        assertEquals(EstadoReserva.EN_CASA, resultado.getEstado());
        assertEquals(EstadoHabitacion.OCUPADA, habitacion.getEstado());
        verify(transaccionFolioService, atLeastOnce()).registrarTransaccion(anyLong(), any(), anyString(), any(BigDecimal.class), anyInt(), any());
    }

    @Test
    void marcar_en_casa_desde_estado_invalido_lanza_excepcion() {
        reserva.setEstado(EstadoReserva.EN_CASA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(EstadoReservaInvalidoException.class,
                () -> reservaService.marcarEnCasa(1L));
    }

    @Test
    void marcar_en_casa_con_fecha_futura_lanza_excepcion() {
        reserva.setFechaEntrada(LocalDate.now().plusDays(10));

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(ValidacionFechasException.class,
                () -> reservaService.marcarEnCasa(1L));
    }

    @Test
    void marcar_en_casa_con_fecha_pasada_lanza_excepcion() {
        reserva.setFechaSalida(LocalDate.now().minusDays(1));

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(ValidacionFechasException.class,
                () -> reservaService.marcarEnCasa(1L));
    }

    @Test
    void marcar_en_casa_habitacion_fuera_servicio_lanza_excepcion() {
        habitacion.setEstado(EstadoHabitacion.FUERA_DE_SERVICIO);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(HabitacionNoDisponibleException.class,
                () -> reservaService.marcarEnCasa(1L));
    }

    @Test
    void marcar_en_casa_habitacion_ocupada_lanza_excepcion() {
        habitacion.setEstado(EstadoHabitacion.OCUPADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(HabitacionNoDisponibleException.class,
                () -> reservaService.marcarEnCasa(1L));
    }

    @Test
    void marcar_en_casa_excede_capacidad_lanza_excepcion() {
        reserva.setCantidadHuespedes(5);  // Capacidad es 2

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(ValidacionFechasException.class,
                () -> reservaService.marcarEnCasa(1L));
    }

    @Test
    void marcar_en_casa_sin_habitacion_asignada_lanza_excepcion() {
        reserva.setHabitacion(null);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(HabitacionNoDisponibleException.class,
                () -> reservaService.marcarEnCasa(1L));
    }

    // ========== CHECK-OUT (REALIZAR CHECKOUT) ==========

    @Test
    void realizar_checkout_desde_estado_invalido_lanza_excepcion() {
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        assertThrows(EstadoReservaInvalidoException.class,
                () -> reservaService.realizarCheckout(1L));
    }

    @Test
    void realizar_checkout_sin_habitacion_asignada_lanza_excepcion() {
        reserva.setEstado(EstadoReserva.EN_CASA);
        reserva.setHabitacion(null);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));
        when(transaccionFolioService.obtenerFolioResumen(1L))
                .thenReturn(new FolioResumenResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));

        assertThrows(HabitacionNoDisponibleException.class,
                () -> reservaService.realizarCheckout(1L));
    }
}
