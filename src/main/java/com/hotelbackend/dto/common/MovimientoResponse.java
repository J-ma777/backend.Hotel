package com.hotelbackend.dto.common;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
        Long id,
        String tipo,        // ENTRADA, SALIDA, AJUSTE
        BigDecimal cantidad,
        String motivo,
        LocalDateTime fecha,

        Long articuloId,
        String articuloNombre
) {}
