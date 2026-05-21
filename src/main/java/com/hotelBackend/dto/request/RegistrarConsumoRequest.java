package com.hotelBackend.dto.request;

public record RegistrarConsumoRequest (
    Long articuloId,
    int cantidad
) {}
