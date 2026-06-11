package com.hotelbackend.dto.request;

import lombok.Data;

@Data
public class CrearHabitacionRequest {
    private String numero;
    private Integer piso;
    private Long tipoHabitacionId;
}
