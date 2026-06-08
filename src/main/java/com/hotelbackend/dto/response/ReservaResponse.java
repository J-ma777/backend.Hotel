package com.hotelbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaResponse {

    private Long id;
    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;

    private Long tipoHabitacionId;
    private String tipoHabitacionNombre;

    private String estado;
    private BigDecimal precioPorNoche;

    private String nombreHuesped;
    private String documentoHuesped;

}
