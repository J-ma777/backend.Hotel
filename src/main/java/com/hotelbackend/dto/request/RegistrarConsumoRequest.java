package com.hotelbackend.dto.request;

public record RegistrarConsumoRequest (
    Long articuloId,
    int cantidad
) {}
