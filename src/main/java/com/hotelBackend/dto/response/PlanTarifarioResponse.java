package com.hotelBackend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PlanTarifarioResponse {

    private Long id;
    private String nombre;
    private BigDecimal precioPorNoche;
    private LocalDate validoDesde;
    private LocalDate validoHasta;
    private Long tipoHabitacionId;
    private String tipoHabitacionNombre;
    private Boolean activo;
}
