package com.hotelBackend.service;

import com.hotelBackend.model.MovimientoInventario;
import com.hotelBackend.model.Usuario;

import java.util.List;

public interface MovimientoInventarioService {

    MovimientoInventario registrarEntrada(Long articuloId, Double cantidad, String motivo);

    MovimientoInventario registrarSalida(Long articuloId, Double cantidad, String motivo);

    List<MovimientoInventario> listarPorArticulo(Long articuloId);

    void registrarConsumo(
            Long reservaId,
            Long articuloId,
            int cantidad,
            Usuario registradoPor
    );
}