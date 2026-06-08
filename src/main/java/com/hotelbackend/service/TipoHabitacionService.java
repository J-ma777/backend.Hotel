package com.hotelbackend.service;

import com.hotelbackend.model.TipoHabitacion;
import java.util.List;

public interface TipoHabitacionService {

    TipoHabitacion guardar(TipoHabitacion tipoHabitacion);

    List<TipoHabitacion> listar();
}
