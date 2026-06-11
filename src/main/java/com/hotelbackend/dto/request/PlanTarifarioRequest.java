package com.hotelbackend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.hotelbackend.model.enums.TipoTarifa;
import lombok.Data;

@Data
public class PlanTarifarioRequest {

    private String nombre;
    private BigDecimal precioPorNoche;
    private LocalDate validoDesde;
    private LocalDate validoHasta;
    private Long tipoHabitacionId;
    private TipoTarifa tipoTarifa;
}