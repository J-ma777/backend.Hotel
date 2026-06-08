package com.hotelbackend.service;

import com.hotelbackend.dto.request.ActualizarHabitacionRequest;
import com.hotelbackend.dto.response.HabitacionMapaResponse;
import com.hotelbackend.dto.response.HabitacionResponse;
import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.enums.EstadoHabitacion;

import java.time.LocalDate;
import java.util.List;

public interface HabitacionService {


    Habitacion guardar(Habitacion habitacion); // Para dar de alta una nueva habitación

    HabitacionResponse actualizar(Long id, ActualizarHabitacionRequest request); // Edicion controlada, se actualiza solo si existe la habitación

    List<HabitacionResponse> listar(); // Vista general de todas las hbitaciones

    Habitacion obtenerPorId(Long id); // Soporte a detalle/ edicion controlada, se obtiene solo si existe la habitación

    void eliminar(Long id); // Administración de habitaciones, se elimina solo si existe la habitación

    List<Habitacion> obtenerParaMantenimiento(); // Método nuevo para obtener habitaciones que necesitan mantenimiento, se llama en el controlador HabitacionController.obtenerParaMantenimiento

    List<HabitacionResponse> obtenerDisponiblesPorTipo(
            Long tipoHabitacionId,
            LocalDate inicio,
            LocalDate fin
    ); // Método nuevo para obtener habitaciones disponibles por tipo, se llama en el controlador HabitacionController.obtenerDisponiblesPorTipo

    Habitacion cambiarEstado(Long id, EstadoHabitacion nuevoEstado);

    List<HabitacionMapaResponse> obtenerMapa();
}
