package com.hotelBackend.dto.request;

import java.math.BigDecimal;

public record MovimientoRequest(
    Long articuloId,
    BigDecimal cantidad,
    String motivo
) {}
