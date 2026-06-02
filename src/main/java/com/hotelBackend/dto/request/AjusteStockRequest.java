package com.hotelBackend.dto.request;

import java.math.BigDecimal;

public record AjusteStockRequest(
        Long articuloId,
        BigDecimal nuevoStock,
        String motivo
) {}
