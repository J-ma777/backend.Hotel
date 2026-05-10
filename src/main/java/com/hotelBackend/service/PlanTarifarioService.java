package com.hotelBackend.service;

import com.hotelBackend.model.PlanTarifario;
import java.util.List;

import java.time.LocalDate;

public interface PlanTarifarioService {

    PlanTarifario obtenerTarifaParaNoche(
            Long tipoHabitacionId,
            LocalDate fechaNoche
    );


    PlanTarifario crear(PlanTarifario plan);

    PlanTarifario actualizar(Long id, PlanTarifario plan);

    List<PlanTarifario> listarTodos();

}
