package com.hotelBackend.service;

import com.hotelBackend.model.ArticuloInventario;
import com.hotelBackend.repository.ArticuloInventarioRepository;
import com.hotelBackend.service.Implementaciones.ArticuloInventarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ArticuloInventarioServiceTest {

    @Mock
    private ArticuloInventarioRepository repository;

    @InjectMocks
    private ArticuloInventarioServiceImpl service;

    public ArticuloInventarioServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void obtenerArticulosConStockMinimo_devuelveOrdenadosPorCriticidad() {

        ArticuloInventario a1 = new ArticuloInventario();
        a1.setNombre("Toalla");
        a1.setStockActual(0.0);
        a1.setStockMinimo(10.0);

        ArticuloInventario a2 = new ArticuloInventario();
        a2.setNombre("Jabón");
        a2.setStockActual(5.0);
        a2.setStockMinimo(10.0);

        when(repository.findByStockActualLessThanEqualStockMinimoOrderByStockActualAsc())
                .thenReturn(List.of(a1, a2));

        List<ArticuloInventario> alertas =
                service.obtenerArticulosConStockMinimo();

        assertThat(alertas).hasSize(2);
        assertThat(alertas.get(0).getNombre()).isEqualTo("Toalla");
    }
}