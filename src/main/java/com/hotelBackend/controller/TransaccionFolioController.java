package com.hotelBackend.controller;

import com.hotelBackend.model.TransaccionFolio;
import com.hotelBackend.model.enums.TipoTransaccion;
import com.hotelBackend.security.util.AuthUtil;
import com.hotelBackend.service.TransaccionFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/folios")
@RequiredArgsConstructor
public class TransaccionFolioController {

    private final TransaccionFolioService transaccionFolioService;

    // REGISTRAR TRANSACCIONES

    @PreAuthorize("hasAuthority('FOLIO_REGISTRAR')")
    @PostMapping("/reservas/{reservaId}/consumos")
    public TransaccionFolio registrarConsumo(
            @PathVariable Long reservaId,
            @RequestParam String descripcion,
            @RequestParam BigDecimal precioUnitario,
            @RequestParam Integer cantidad
    ) {
        return transaccionFolioService.registrarTransaccion(
                reservaId,
                TipoTransaccion.CARGO_CONSUMO,
                descripcion,
                precioUnitario,
                cantidad,
                AuthUtil.getCurrentUserId()
        );
    }

    @PreAuthorize("hasAuthority('FOLIO_REGISTRAR')")
    @PostMapping("/reservas/{reservaId}/pagos")
    public TransaccionFolio registrarPago(
            @PathVariable Long reservaId,
            @RequestParam BigDecimal monto
    ) {
        return transaccionFolioService.registrarTransaccion(
                reservaId,
                TipoTransaccion.PAGO,
                "Pago recibido",
                monto,
                1,
                AuthUtil.getCurrentUserId()
        );
    }

    @PreAuthorize("hasAuthority('FOLIO_REGISTRAR')")
    @PostMapping("/reservas/{reservaId}/descuentos")
    public TransaccionFolio registrarDescuento(
            @PathVariable Long reservaId,
            @RequestParam String descripcion,
            @RequestParam BigDecimal monto
    ) {
        return transaccionFolioService.registrarTransaccion(
                reservaId,
                TipoTransaccion.DESCUENTO,
                descripcion,
                monto,
                1,
                AuthUtil.getCurrentUserId()
        );
    }

    // CONSULTAS

    @PreAuthorize("hasAuthority('FOLIO_VER')")
    @GetMapping("/reservas/{reservaId}/transacciones")
    public List<TransaccionFolio> obtenerTransacciones(
            @PathVariable Long reservaId
    ) {
        return transaccionFolioService.obtenerTransaccionesPorReserva(reservaId);
    }

    @PreAuthorize("hasAuthority('FOLIO_VER')")
    @GetMapping("/reservas/{reservaId}/saldo")
    public BigDecimal obtenerSaldo(
            @PathVariable Long reservaId
    ) {
        return transaccionFolioService.obtenerSaldoReserva(reservaId);
    }
}