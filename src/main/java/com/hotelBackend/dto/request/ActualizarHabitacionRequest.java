package com.hotelBackend.dto.request;

import lombok.Data;

@Data
public class ActualizarHabitacionRequest {

    private String numero;
    private Integer piso;
    private Long tipoHabitacionId;

}
