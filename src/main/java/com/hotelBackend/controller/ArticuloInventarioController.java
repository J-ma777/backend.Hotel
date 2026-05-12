package com.hotelBackend.controller;

import com.hotelBackend.model.ArticuloInventario;
import com.hotelBackend.service.ArticuloInventarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario/articulos")
public class ArticuloInventarioController {

    private final ArticuloInventarioService articuloInventarioService;

    public ArticuloInventarioController(
            ArticuloInventarioService articuloInventarioService) {
        this.articuloInventarioService = articuloInventarioService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR') or hasRole('ADMIN')")
    public ArticuloInventario crear(@RequestBody ArticuloInventario articulo) {
        return articuloInventarioService.crear(articulo);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public List<ArticuloInventario> listarTodos() {
        return articuloInventarioService.listarTodos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public ArticuloInventario obtenerPorId(@PathVariable Long id) {
        return articuloInventarioService.obtenerPorId(id);
    }
}