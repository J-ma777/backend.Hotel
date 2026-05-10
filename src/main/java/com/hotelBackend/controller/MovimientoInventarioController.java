package com.hotelBackend.controller;

import com.hotelBackend.model.ArticuloInventario;
import com.hotelBackend.model.MovimientoInventario;
import com.hotelBackend.service.MovimientoInventarioService;
import com.hotelBackend.service.ArticuloInventarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;
    private final ArticuloInventarioService articuloService;

    public MovimientoInventarioController(
            MovimientoInventarioService movimientoService,
            ArticuloInventarioService articuloService
    ) {
        this.movimientoService = movimientoService;
        this.articuloService = articuloService;
    }

    @PostMapping("/{articuloId}/entrada")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    public MovimientoInventario registrarEntrada(
            @PathVariable Long articuloId,
            @RequestParam Double cantidad,
            @RequestParam(required = false) String motivo
    ) {
        return movimientoService.registrarEntrada(articuloId, cantidad, motivo);
    }

    @PostMapping("/{articuloId}/salida")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    public MovimientoInventario registrarSalida(
            @PathVariable Long articuloId,
            @RequestParam Double cantidad,
            @RequestParam(required = false) String motivo
    ) {
        return movimientoService.registrarSalida(articuloId, cantidad, motivo);
    }

    @GetMapping("/articulo/{articuloId}")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public List<MovimientoInventario> listarMovimientos(@PathVariable Long articuloId) {
        return movimientoService.listarPorArticulo(articuloId);
    }

    // Alertas de stcck mínimo: Devuelve los artículos cuyo
    // stock actual es menor o igual al stock mínimo, indicando que necesitan reposición
    @GetMapping("/alertas/stock-minimo")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public List<ArticuloInventario> alertasStockMinimo() {
        return articuloService.obtenerArticulosConStockMinimo();
    }
}