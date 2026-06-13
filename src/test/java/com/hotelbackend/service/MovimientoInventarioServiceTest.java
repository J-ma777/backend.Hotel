package com.hotelbackend.service;

import com.hotelbackend.model.ArticuloInventario;
import com.hotelbackend.model.MovimientoInventario;
import com.hotelbackend.model.enums.TipoMovimiento;
import com.hotelbackend.repository.ArticuloInventarioRepository;
import com.hotelbackend.repository.MovimientoInventarioRepository;
import com.hotelbackend.service.implementaciones.MovimientoInventarioServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MovimientoInventarioServiceTest {

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @Mock
    private ArticuloInventarioRepository articuloRepository;

    @Mock
    private TransaccionFolioService transaccionFolioService; // FALTABA

    @InjectMocks
    private MovimientoInventarioServiceImpl service;

    private ArticuloInventario articulo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Simula usuario autenticado
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("1", null)
        );

        articulo = new ArticuloInventario();
        articulo.setId(1L);
        articulo.setNombre("Papel higiénico");
        articulo.setStockActual(BigDecimal.valueOf(20.0));
        articulo.setStockMinimo(BigDecimal.valueOf(10.0));
    }

    //Entrada de nuevo stock al inventario no afecta folio
    @Test
    void registrarEntrada_incrementaStock_yCreaMovimiento() {

        when(articuloRepository.findById(1L))
                .thenReturn(Optional.of(articulo));

        when(movimientoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        MovimientoInventario movimiento =
                service.registrarEntrada(1L, BigDecimal.valueOf(5.0), "Compra proveedor");

        assertThat(articulo.getStockActual())
                .isEqualByComparingTo(BigDecimal.valueOf(25.0));

        assertThat(movimiento.getTipo())
                .isEqualTo(TipoMovimiento.ENTRADA);

        assertThat(movimiento.getCantidad())
                .isEqualByComparingTo(BigDecimal.valueOf(5.0));

        assertThat(movimiento.getRegistradoPor())
                .isEqualTo(1L);

        verify(movimientoRepository).save(any());
        verify(articuloRepository).save(articulo);
    }

    // Salida de stock por consumo reduce el stock y crea movimiento
    @Test
    void registrarSalida_valida_reduceStock_yCreaMovimiento() {

        when(articuloRepository.findById(1L))
                .thenReturn(Optional.of(articulo));

        when(movimientoRepository.save(any()))
                .thenAnswer(i -> i.getArgument(0));

        MovimientoInventario movimiento =
                service.registrarSalida(1L, BigDecimal.valueOf(10.0), "Consumo habitación 101");

        assertThat(articulo.getStockActual())
                .isEqualByComparingTo(BigDecimal.valueOf(10.0));

        assertThat(movimiento.getTipo())
                .isEqualTo(TipoMovimiento.SALIDA);

        verify(movimientoRepository).save(any());
    }

    // Stock insuficiente no se debe crear movimiento ni modificar stock
    @Test
    void registrarSalida_stockInsuficiente_lanzaExcepcion() {

        when(articuloRepository.findById(1L))
                .thenReturn(Optional.of(articulo));

        assertThatThrownBy(() ->
                service.registrarSalida(1L, BigDecimal.valueOf(30.0), "Error")
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock insuficiente");

        verify(movimientoRepository, never()).save(any());
    }
}