package com.hotelbackend.service;

import com.hotelbackend.dto.request.PlanTarifarioRequest;
import com.hotelbackend.dto.response.PlanTarifarioResponse;
import com.hotelbackend.model.PlanTarifario;
import java.util.List;

import java.time.LocalDate;

public interface PlanTarifarioService {

    PlanTarifario obtenerTarifaParaNoche(
            Long tipoHabitacionId,
            LocalDate fechaNoche
    );


    PlanTarifarioResponse crear(PlanTarifarioRequest request);

    PlanTarifarioResponse actualizar(Long id, PlanTarifarioRequest request);

    List<PlanTarifarioResponse> listarTodos();

    List<PlanTarifarioResponse> listarPorTipo(Long tipoHabitacionId);

}
