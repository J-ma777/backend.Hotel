package com.hotelBackend.service;


import com.hotelBackend.exception.ArticuloNoEncontradoException;
import com.hotelBackend.exception.ReservaNoEnCasaException;
import com.hotelBackend.exception.StockInsuficienteException;
import com.hotelBackend.model.*;
import com.hotelBackend.model.enums.EstadoReserva;
import com.hotelBackend.model.enums.TipoTransaccion;
import com.hotelBackend.repository.ArticuloInventarioRepository;
import com.hotelBackend.repository.MovimientoInventarioRepository;
import com.hotelBackend.repository.ReservaRepository;
import com.hotelBackend.repository.TransaccionFolioRepository;
import com.hotelBackend.service.Implementaciones.TransaccionFolioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransaccionFolioServiceImpl - Lógica financiera del PMS")
public class TransaccionFolioServiceImplTest {

    @Mock
    private TransaccionFolioRepository transaccionFolioRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private ArticuloInventarioRepository articuloInventarioRepository;

    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;

    @InjectMocks
    private TransaccionFolioServiceImpl transaccionFolioService;

    private Reserva reserva;

    @BeforeEach
    void setUp() {
        reserva = new Reserva();
        reserva.setId(1L);
    }

    @Test
    @DisplayName("Registrar CARGO_CONSUMO calcula total correctamente")
    void registrarCargoConsumo_calculaTotal() {

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));
        when(transaccionFolioRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransaccionFolio tx = transaccionFolioService.registrarTransaccion(
                1L,
                TipoTransaccion.CARGO_CONSUMO,
                "Minibar",
                new BigDecimal("15.00"),
                2,
                10L
        );

        assertThat(tx.getTotal()).isEqualByComparingTo("30.00");
        assertThat(tx.getTipo()).isEqualTo(TipoTransaccion.CARGO_CONSUMO);
    }

    @Test
    @DisplayName("Registrar PAGO se guarda como transacción PAGO")
    void registrarPago_ok() {

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));
        when(transaccionFolioRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransaccionFolio tx = transaccionFolioService.registrarTransaccion(
                1L,
                TipoTransaccion.PAGO,
                "Pago en efectivo",
                new BigDecimal("100.00"),
                1,
                10L
        );

        assertThat(tx.getTotal()).isEqualByComparingTo("100.00");
        assertThat(tx.getTipo()).isEqualTo(TipoTransaccion.PAGO);
    }

    @Test
    @DisplayName("Obtener saldo: cargos suman, pagos restan")
    void obtenerSaldoReserva_calculaCorrectamente() {

        TransaccionFolio cargoNoche = new TransaccionFolio();
        cargoNoche.setTipo(TipoTransaccion.CARGO_NOCHE);
        cargoNoche.setTotal(new BigDecimal("200.00"));

        TransaccionFolio consumo = new TransaccionFolio();
        consumo.setTipo(TipoTransaccion.CARGO_CONSUMO);
        consumo.setTotal(new BigDecimal("50.00"));

        TransaccionFolio pago = new TransaccionFolio();
        pago.setTipo(TipoTransaccion.PAGO);
        pago.setTotal(new BigDecimal("100.00"));

        when(transaccionFolioRepository.findByReservaId(1L))
                .thenReturn(List.of(cargoNoche, consumo, pago));

        BigDecimal saldo = transaccionFolioService.obtenerSaldoReserva(1L);

        assertThat(saldo).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("Registrar consumo: descuenta inventario y genera CARGO_CONSUMO")
    void registrarConsumo_descuentaInventarioYGeneraTransaccion() {

        // GIVEN
        reserva.setEstado(EstadoReserva.EN_CASA);

        Habitacion habitacion = new Habitacion();
        habitacion.setNumero("101");
        reserva.setHabitacion(habitacion);

        ArticuloInventario articulo = new ArticuloInventario();
        articulo.setId(5L);
        articulo.setNombre("Agua Minibar");
        articulo.setStockActual(10.0);
        articulo.setCostoUnitario(new BigDecimal("5.00"));

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(articuloInventarioRepository.findById(5L))
                .thenReturn(Optional.of(articulo));

        when(transaccionFolioRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TransaccionFolio tx = transaccionFolioService.registrarConsumo(
                1L,
                5L,
                2,
                10L
        );

        // THEN
        assertThat(tx.getTipo()).isEqualTo(TipoTransaccion.CARGO_CONSUMO);
        assertThat(tx.getCantidad()).isEqualTo(2);
        assertThat(tx.getTotal()).isEqualByComparingTo("10.00");
        assertThat(tx.getRegistradoPor()).isEqualTo(10L);

        assertThat(articulo.getStockActual()).isEqualTo(8);

        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
        verify(articuloInventarioRepository).save(articulo);
        verify(transaccionFolioRepository).save(any(TransaccionFolio.class));
    }

    @Test
    @DisplayName("Registrar consumo descuenta inventario y genera CARGO_CONSUMO")
    void registrarConsumo_ok() {

        // GIVEN
        reserva.setEstado(EstadoReserva.EN_CASA);

        Habitacion habitacion = new Habitacion();
        habitacion.setNumero("101");
        reserva.setHabitacion(habitacion);

        ArticuloInventario articulo = new ArticuloInventario();
        articulo.setId(5L);
        articulo.setNombre("Agua Minibar");
        articulo.setStockActual(10.0);
        articulo.setCostoUnitario(new BigDecimal("5.00"));

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(articuloInventarioRepository.findById(5L))
                .thenReturn(Optional.of(articulo));

        when(transaccionFolioRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // WHEN
        TransaccionFolio tx = transaccionFolioService.registrarConsumo(
                1L,
                5L,
                2,
                10L
        );

        // THEN
        assertThat(tx.getTipo()).isEqualTo(TipoTransaccion.CARGO_CONSUMO);
        assertThat(tx.getCantidad()).isEqualTo(2);
        assertThat(tx.getTotal()).isEqualByComparingTo("10.00");
        assertThat(articulo.getStockActual()).isEqualTo(8.0);

        verify(movimientoInventarioRepository).save(any(MovimientoInventario.class));
    }

    @Test
    @DisplayName("No permite consumo si la reserva no está EN_CASA")
    void registrarConsumo_reservaNoEnCasa() {

        // GIVEN
        reserva.setEstado(EstadoReserva.CONFIRMADA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        // THEN
        assertThatThrownBy(() ->
                transaccionFolioService.registrarConsumo(
                        1L,
                        5L,
                        1,
                        10L
                )
        ).isInstanceOf(ReservaNoEnCasaException.class);
    }

    @Test
    @DisplayName("No permite consumo si no hay stock suficiente")
    void registrarConsumo_stockInsuficiente() {

        // GIVEN
        reserva.setEstado(EstadoReserva.EN_CASA);

        ArticuloInventario articulo = new ArticuloInventario();
        articulo.setStockActual(1.0);
        articulo.setCostoUnitario(new BigDecimal("5.00"));

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(articuloInventarioRepository.findById(5L))
                .thenReturn(Optional.of(articulo));

        // THEN
        assertThatThrownBy(() ->
                transaccionFolioService.registrarConsumo(
                        1L,
                        5L,
                        2,
                        10L
                )
        ).isInstanceOf(StockInsuficienteException.class);
    }

    @Test
    @DisplayName("No permite consumo si el artículo no existe")
    void registrarConsumo_articuloNoExiste() {

        reserva.setEstado(EstadoReserva.EN_CASA);

        when(reservaRepository.findById(1L))
                .thenReturn(Optional.of(reserva));

        when(articuloInventarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                transaccionFolioService.registrarConsumo(
                        1L,
                        99L,
                        1,
                        10L
                )
        ).isInstanceOf(ArticuloNoEncontradoException.class);
    }
}
