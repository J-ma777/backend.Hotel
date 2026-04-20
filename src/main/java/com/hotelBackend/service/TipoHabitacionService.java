package com.hotelBackend.service;

import com.hotelBackend.model.TipoHabitacion;
import java.util.List;

public interface TipoHabitacionService {

    TipoHabitacion guardar(TipoHabitacion tipoHabitacion);

    List<TipoHabitacion> listar();
}
