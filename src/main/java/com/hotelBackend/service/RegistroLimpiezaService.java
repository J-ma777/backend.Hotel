package com.hotelBackend.service;

import com.hotelBackend.model.RegistroLimpieza;
import com.hotelBackend.model.enums.EstadoHabitacion;
import java.util.List;

public interface RegistroLimpiezaService {

    RegistroLimpieza registrarCambioEstado(
            Long habitacionId,
            EstadoHabitacion estadoNuevo,
            String notas,
            Long usuarioId
    );

    List<RegistroLimpieza> listarPorHabitacion(Long habitacionId);

}
