package com.hotelbackend.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class HabitacionMapaResponse {

    private Long id;
    private String numero;
    private String estado;
    private Integer piso;
    private String tipoNombre;

    // NUEVO (lo importante)
    private String nombreHuesped;
    private Integer cantidadHuespedes;
    private LocalDate fechaSalida;
    private Integer capacidad;
    private Long reservaId;
    private Long tipoId;
}
