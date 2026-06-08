package com.hotelbackend.controller;

import com.hotelbackend.dto.common.MovimientoResponse;
import com.hotelbackend.dto.request.AjusteStockRequest;
import com.hotelbackend.dto.request.MovimientoRequest;
import com.hotelbackend.model.ArticuloInventario;
import com.hotelbackend.model.MovimientoInventario;
import com.hotelbackend.service.MovimientoInventarioService;
import com.hotelbackend.service.ArticuloInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;
    private final ArticuloInventarioService articuloService;

    @PostMapping("/entrada")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    public ResponseEntity<MovimientoResponse> registrarEntrada(
            @RequestBody MovimientoRequest request
    ) {
        MovimientoInventario mov = movimientoService.registrarEntrada(
                request.articuloId(),
                request.cantidad(),
                request.motivo()
        );

        return ResponseEntity.ok(mapToResponse(mov));
    }

    @PostMapping("/salida")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    public ResponseEntity<MovimientoResponse> registrarSalida(
            @RequestBody MovimientoRequest request
    ) {
        MovimientoInventario mov = movimientoService.registrarSalida(
                request.articuloId(),
                request.cantidad(),
                request.motivo()
        );

        return ResponseEntity.ok(mapToResponse(mov));
    }

    @PostMapping("/ajuste")
    @PreAuthorize("hasAuthority('INVENTARIO_GESTIONAR')")
    public ResponseEntity<MovimientoResponse> ajustar(
            @RequestBody AjusteStockRequest request
    ) {
        MovimientoInventario mov = movimientoService.ajustarStock(
                request.articuloId(),
                request.nuevoStock(),
                request.motivo()
        );

        return ResponseEntity.ok(mapToResponse(mov));
    }

    @GetMapping("/articulo/{articuloId}")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public ResponseEntity<List<MovimientoResponse>> listarMovimientos(
            @PathVariable Long articuloId
    ) {
        List<MovimientoResponse> response = movimientoService
                .listarPorArticulo(articuloId)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/alertas/stock-minimo")
    @PreAuthorize("hasAuthority('INVENTARIO_VER')")
    public ResponseEntity<List<ArticuloInventario>> alertasStockMinimo() {
        return ResponseEntity.ok(
                articuloService.obtenerArticulosConStockMinimo()
        );
    }

    private MovimientoResponse mapToResponse(MovimientoInventario m) {
        return new MovimientoResponse(
                m.getId(),
                m.getTipo().name(),
                m.getCantidad(),
                m.getMotivo(),
                m.getFechaMovimiento(),
                m.getArticulo().getId(),
                m.getArticulo().getNombre()
        );
    }
}