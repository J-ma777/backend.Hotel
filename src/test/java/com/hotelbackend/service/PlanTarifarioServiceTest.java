package com.hotelbackend.service;

import com.hotelbackend.model.PlanTarifario;
import com.hotelbackend.repository.PlanTarifarioRepository;
import com.hotelbackend.service.Implementaciones.PlanTarifarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlanTarifarioServiceTest {

    @Mock
    private PlanTarifarioRepository repository;

    @InjectMocks
    private PlanTarifarioServiceImpl service;

    // Prioriad 1: Feriado y esa nota
    @Test
    void devuelve_tarifa_de_feriado_con_prioridad() {
        LocalDate fecha = LocalDate.of(2026, 7, 28);

        PlanTarifario feriado = new PlanTarifario();
        feriado.setNombre("Feriado");

        when(repository.buscarPlanes(anyLong(), eq(fecha), eq(true), eq(false)))
                .thenReturn(List.of(feriado));

        PlanTarifario resultado =
                service.obtenerTarifaParaNoche(1L, fecha);

        assertEquals("Feriado", resultado.getNombre());
    }

    @Test
    void lanza_excepcion_si_no_hay_tarifa() {
        LocalDate fecha = LocalDate.of(2026, 7, 29);

        when(repository.buscarPlanes(anyLong(), eq(fecha), any(), any()))
                .thenReturn(List.of());

        assertThrows(IllegalStateException.class, () ->
                service.obtenerTarifaParaNoche(1L, fecha));
    }
}
