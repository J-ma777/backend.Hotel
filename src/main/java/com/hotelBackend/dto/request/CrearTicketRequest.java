package com.hotelBackend.dto.request;

import lombok.Data;

@Data
public class CrearTicketRequest {

    private Long habitacionId;
    private String descripcion;
}
