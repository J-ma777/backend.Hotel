package com.hotelBackend.service;

import com.hotelBackend.model.Habitacion;
import java.util.List;

public interface HabitacionService {


    Habitacion guardar(Habitacion habitacion); // Para dar de alta una nueva habitación

    Habitacion actualizar(Long id, Habitacion habitacion); // Edicion controlada, se actualiza solo si existe la habitación

    List<Habitacion> listar(); // Vista general de todas las hbitaciones

    Habitacion obtenerPorId(Long id); // Soporte a detalle/ edicion controlada, se obtiene solo si existe la habitación

    void eliminar(Long id); // Administración de habitaciones, se elimina solo si existe la habitación

}
