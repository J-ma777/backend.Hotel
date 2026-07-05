package com.hotelbackend.controller;

import com.hotelbackend.dto.response.FolioResumenResponse;
import com.hotelbackend.model.TransaccionFolio;
import com.hotelbackend.model.enums.TipoTransaccion;
import com.hotelbackend.security.util.AuthUtil;
import com.hotelbackend.service.TransaccionFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
            @RequestBody Map<String, Object> payload
    )
    {
        BigDecimal monto = new BigDecimal(payload.get("monto").toString());

        String descripcion =
                payload.get("descripcion") != null
                        ? payload.get("descripcion").toString()
                        : "Salida anticipada";

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


    @PreAuthorize("hasAuthority('FOLIO_VER')")
    @GetMapping("/reservas/{reservaId}/resumen")
    public FolioResumenResponse obtenerResumen(
            @PathVariable Long reservaId
    ) {
        return transaccionFolioService.obtenerFolioResumen(reservaId);
    }
}