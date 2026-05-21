package com.hotelBackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HabitacionResponse {

    private Long id;
    private String numero;
    private String estado;
    private Integer piso;
    private String tipoNombre;
}
