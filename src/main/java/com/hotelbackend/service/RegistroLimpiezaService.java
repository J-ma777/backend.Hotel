package com.hotelbackend.service;

import com.hotelbackend.model.RegistroLimpieza;
import com.hotelbackend.model.enums.EstadoHabitacion;
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
