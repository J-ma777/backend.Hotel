package com.hotelBackend.service;

import com.hotelBackend.model.ArticuloInventario;
import com.hotelBackend.model.MovimientoInventario;
import com.hotelBackend.model.enums.TipoMovimiento;
import com.hotelBackend.repository.ArticuloInventarioRepository;
import com.hotelBackend.repository.MovimientoInventarioRepository;
import com.hotelBackend.service.Implementaciones.MovimientoInventarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovimientoInventarioServiceTest {

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @Mock
    private ArticuloInventarioRepository articuloRepository;

    @InjectMocks
    private MovimientoInventarioServiceImpl service;

    private ArticuloInventario articulo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simula usuario autenticado (registradoPor)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null)
        );

        articulo = new ArticuloInventario();
        articulo.setId(1L);
        articulo.setNombre("Papel higiénico");
        articulo.setStockActual(20.0);
        articulo.setStockMinimo(10.0);
    }

    @Test
    void registrarEntrada_incrementaStock_yCreaMovimiento() {

        when(articuloRepository.findById(1L))
                .thenReturn(Optional.of(articulo));

        when(movimientoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        MovimientoInventario movimiento =
                service.registrarEntrada(1L, 5.0, "Compra proveedor");

        assertThat(articulo.getStockActual()).isEqualTo(25.0);
        assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.ENTRADA);
        assertThat(movimiento.getCantidad()).isEqualTo(5.0);
        assertThat(movimiento.getRegistradoPor()).isEqualTo(1L);

        verify(movimientoRepository).save(any());
        verify(articuloRepository).save(articulo);
    }

    @Test
    void registrarSalida_valida_reduceStock_yCreaMovimiento() {

        when(articuloRepository.findById(1L))
                .thenReturn(Optional.of(articulo));

        when(movimientoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        MovimientoInventario movimiento =
                service.registrarSalida(1L, 10.0, "Consumo habitación 101");

        assertThat(articulo.getStockActual()).isEqualTo(10.0);
        assertThat(movimiento.getTipo()).isEqualTo(TipoMovimiento.SALIDA);

        verify(movimientoRepository).save(any());
    }

    @Test
    void registrarSalida_stockInsuficiente_lanzaExcepcion() {

        when(articuloRepository.findById(1L))
                .thenReturn(Optional.of(articulo));

        assertThatThrownBy(() ->
                service.registrarSalida(1L, 30.0, "Error")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(movimientoRepository, never()).save(any());
    }
}