package com.hotelBackend.service;

import com.hotelBackend.model.TransaccionFolio;
import com.hotelBackend.model.enums.TipoTransaccion;

import java.math.BigDecimal;
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

}
