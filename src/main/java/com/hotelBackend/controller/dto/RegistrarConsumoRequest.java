package com.hotelBackend.controller.dto;

public record RegistrarConsumoRequest (
    Long articuloId,
    int cantidad
) {}
