package com.hotelBackend.service;

import com.hotelBackend.model.ArticuloInventario;
import java.util.List;

public interface ArticuloInventarioService {


    ArticuloInventario crear(ArticuloInventario articulo);

    List<ArticuloInventario> listarTodos();

    ArticuloInventario obtenerPorId(Long id);

    // Devuleve los artículos cuyo stock actual es menor o igual al stock mínimo, indicando que necesitan reposición
    List<ArticuloInventario> obtenerArticulosConStockMinimo();
}
