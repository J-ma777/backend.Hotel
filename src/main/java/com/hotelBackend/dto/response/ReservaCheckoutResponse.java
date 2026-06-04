package com.hotelBackend.dto.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ReservaCheckoutResponse {

    private Long id;

    private String nombreHuesped;
    private String estado;

    private LocalDate fechaEntrada;
    private LocalDate fechaSalida;

    private Long habitacionId;
    private String habitacionNumero;

}
