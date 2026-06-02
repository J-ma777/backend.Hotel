package com.hotelBackend.service;

import com.hotelBackend.dto.request.PlanTarifarioRequest;
import com.hotelBackend.dto.response.PlanTarifarioResponse;
import com.hotelBackend.model.PlanTarifario;
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
