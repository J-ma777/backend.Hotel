package com.hotelBackend.service;

import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.TipoHabitacion;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.service.Implementaciones.HabitacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitacionServiceImplTest {

    @Mock
    private HabitacionRepository habitacionRepository;

    @InjectMocks
    private HabitacionServiceImpl habitacionService;

    private TipoHabitacion tipoHabitacion;
    private Habitacion habitacion;

    @BeforeEach
    void setUp() {
        tipoHabitacion = new TipoHabitacion(1L, "Suite", "Habitación de lujo", 2);

        habitacion = new Habitacion();
        habitacion.setId(1L);
        habitacion.setNumero("101");
        habitacion.setPiso(1);
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacion.setTipoHabitacion(tipoHabitacion);
    }

    // TEST 1: guardar() persiste y retorna la habitación correctamente que se este pidiendo guardar

    @Test
    @DisplayName("Test 1 - guardar: debe persistir y retornar la habitación")
    void guardar_debeRetornarHabitacionGuardada() {
        // Arrange (organizar el escenario)
        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(habitacion);

        // Act (actuar- ejecutar el método a probar)
        Habitacion resultado = habitacionService.guardar(habitacion);

        // Assert (afirmar)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumero()).isEqualTo("101");
        assertThat(resultado.getPiso()).isEqualTo(1);
        assertThat(resultado.getEstado()).isEqualTo(EstadoHabitacion.DISPONIBLE);
        verify(habitacionRepository, times(1)).save(habitacion);
    }

    // TEST 2: listar() retorna todas las habitaciones existentes

    @Test
    @DisplayName("Test 2 - listar: debe retornar la lista completa de habitaciones")
    void listar_debeRetornarTodasLasHabitaciones() {
        // Arrange (organizar)
        Habitacion habitacion2 = new Habitacion();
        habitacion2.setId(2L);
        habitacion2.setNumero("202");
        habitacion2.setPiso(2);
        habitacion2.setEstado(EstadoHabitacion.OCUPADA);
        habitacion2.setTipoHabitacion(tipoHabitacion);

        when(habitacionRepository.findAll()).thenReturn(List.of(habitacion, habitacion2));

        // Act (actuar)
        List<Habitacion> resultado = habitacionService.listar();

        // Assert (afirmar)
        assertThat(resultado).isNotNull();
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNumero()).isEqualTo("101");
        assertThat(resultado.get(1).getNumero()).isEqualTo("202");
        verify(habitacionRepository, times(1)).findAll();
    }

    // TEST 3: obtenerPorId() retorna la habitación cuando el ID existe

    @Test
    @DisplayName("Test 3 - obtenerPorId: debe retornar la habitación si el ID existe")
    void obtenerPorId_cuandoIdExiste_debeRetornarHabitacion() {
        // Arrange (organizar)
        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));

        // Act (actuar)
        Habitacion resultado = habitacionService.obtenerPorId(1L);

        // Assert (afirmar)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNumero()).isEqualTo("101");
        verify(habitacionRepository, times(1)).findById(1L);
    }

    // TEST 4: obtenerPorId() lanza RuntimeException cuando el ID no existe

    @Test
    @DisplayName("Test 4 - obtenerPorId: debe lanzar RuntimeException si el ID no existe")
    void obtenerPorId_cuandoIdNoExiste_debeLanzarExcepcion() {
        // Arrange (organizar)
        when(habitacionRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert (Actuar y afirmar)
        assertThatThrownBy(() -> habitacionService.obtenerPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Habitación no encontrada");

        verify(habitacionRepository, times(1)).findById(99L);
    }

    // TEST 5: actualizar() modifica los campos y retorna la habitación actualizada

    @Test
    @DisplayName("Test 5 - actualizar: debe modificar los campos y retornar la habitación actualizada")
    void actualizar_cuandoIdExiste_debeActualizarYRetornarHabitacion() {
        // Arrange (organizar)
        Habitacion datosNuevos = new Habitacion();
        datosNuevos.setNumero("101-A");
        datosNuevos.setPiso(3);
        datosNuevos.setEstado(EstadoHabitacion.FUERA_DE_SERVICIO);
        datosNuevos.setTipoHabitacion(tipoHabitacion);

        Habitacion habitacionActualizada = new Habitacion();
        habitacionActualizada.setId(1L);
        habitacionActualizada.setNumero("101-A");
        habitacionActualizada.setPiso(3);
        habitacionActualizada.setEstado(EstadoHabitacion.FUERA_DE_SERVICIO);
        habitacionActualizada.setTipoHabitacion(tipoHabitacion);

        when(habitacionRepository.findById(1L)).thenReturn(Optional.of(habitacion));
        when(habitacionRepository.save(any(Habitacion.class))).thenReturn(habitacionActualizada);

        // Act (actuar)
        Habitacion resultado = habitacionService.actualizar(1L, datosNuevos);

        // Assert (afirmar)
        assertThat(resultado).isNotNull();
        assertThat(resultado.getNumero()).isEqualTo("101-A");
        assertThat(resultado.getPiso()).isEqualTo(3);
        assertThat(resultado.getEstado()).isEqualTo(EstadoHabitacion.FUERA_DE_SERVICIO);
        verify(habitacionRepository, times(1)).findById(1L);
        verify(habitacionRepository, times(1)).save(any(Habitacion.class));
    }

    @Test
    @DisplayName("Test 6 - eliminar: debe eliminar la habitación si el ID existe")
    void eliminar_debeEliminarHabitacion() {
        // Act (actuar)
        habitacionService.eliminar(1L);

        // Assert (afirmar)
        verify(habitacionRepository, times(1)).deleteById(1L);
    }
}