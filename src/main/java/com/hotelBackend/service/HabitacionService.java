package com.hotelBackend.service;

import com.hotelBackend.dto.response.HabitacionResponse;
import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.enums.EstadoReserva;

import java.time.LocalDate;
import java.util.List;

public interface HabitacionService {


    Habitacion guardar(Habitacion habitacion); // Para dar de alta una nueva habitación

    Habitacion actualizar(Long id, Habitacion habitacion); // Edicion controlada, se actualiza solo si existe la habitación

    List<HabitacionResponse> listar(); // Vista general de todas las hbitaciones

    Habitacion obtenerPorId(Long id); // Soporte a detalle/ edicion controlada, se obtiene solo si existe la habitación

    void eliminar(Long id); // Administración de habitaciones, se elimina solo si existe la habitación

    List<Habitacion> obtenerParaMantenimiento(); // Método nuevo para obtener habitaciones que necesitan mantenimiento, se llama en el controlador HabitacionController.obtenerParaMantenimiento

    List<HabitacionResponse> obtenerDisponiblesPorTipo(
            Long tipoHabitacionId,
            LocalDate inicio,
            LocalDate fin
    ); // Método nuevo para obtener habitaciones disponibles por tipo, se llama en el controlador HabitacionController.obtenerDisponiblesPorTipo
}
