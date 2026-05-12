package com.hotelBackend.controller;

import com.hotelBackend.controller.dto.RegistrarConsumoRequest;
import com.hotelBackend.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ConsumoController {

    private final MovimientoInventarioService movimientoInventarioService;

    @PostMapping("/{reservaId}/consumos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('REGISTRAR_CONSUMO')")
    public void registrarConsumo(
            @PathVariable Long reservaId,
            @Valid @RequestBody RegistrarConsumoRequest request
    ) {
        movimientoInventarioService.registrarConsumo(
                reservaId,
                request.articuloId(),
                request.cantidad(),
                null
        );
    }

}
