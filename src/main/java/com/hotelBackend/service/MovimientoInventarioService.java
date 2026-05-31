package com.hotelBackend.service;

import com.hotelBackend.model.MovimientoInventario;

import java.math.BigDecimal;
import java.util.List;

public interface MovimientoInventarioService {

    MovimientoInventario registrarEntrada(Long articuloId, BigDecimal cantidad, String motivo);

    MovimientoInventario registrarSalida(Long articuloId, BigDecimal cantidad, String motivo);

    MovimientoInventario ajustarStock(Long articuloId, BigDecimal nuevoStock, String motivo);

    List<MovimientoInventario> listarPorArticulo(Long articuloId);

    void registrarConsumo(
            Long reservaId,
            Long articuloId,
            int cantidad,
            Long registradoPorId
    );
}