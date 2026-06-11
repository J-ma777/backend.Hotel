package com.hotelbackend.service;

import com.hotelbackend.dto.response.FolioResumenResponse;
import com.hotelbackend.model.TransaccionFolio;
import com.hotelbackend.model.enums.TipoTransaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransaccionFolioService {

    TransaccionFolio registrarTransaccion(
            Long reservaId,
            TipoTransaccion tipo,
            String descripcion,
            BigDecimal precioUnitario,
            Integer cantidad,
            Long registradoPor
    );

    List<TransaccionFolio> obtenerTransaccionesPorReserva(Long reservaId);

    BigDecimal obtenerSaldoReserva(Long reservaId);

    // Registrar un comsumo a su cuenta
    TransaccionFolio registrarConsumo(
            Long reservaId,
            Long articuloId,
            int cantidad,
            Long registradoPor
    );

    FolioResumenResponse obtenerFolioResumen(Long reservaId);

    BigDecimal obtenerIngresos(LocalDate inicio, LocalDate fin);
}
