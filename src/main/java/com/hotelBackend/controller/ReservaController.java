package com.hotelBackend.controller;

import com.hotelBackend.controller.dto.CrearReservaRequest;
import com.hotelBackend.model.Reserva;
import com.hotelBackend.security.util.AuthUtil;
import com.hotelBackend.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PreAuthorize("hasAuthority('RESERVA_VER')")
    @GetMapping
    public List<Reserva> listar() {
        return reservaService.listar();
    }

    @PreAuthorize("hasAuthority('RESERVA_VER')")
    @GetMapping("/{id}")
    public Reserva obtenerPorId(@PathVariable Long id) {
        return reservaService.obtenerPorId(id);
    }

    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @PostMapping
    public Reserva crear(@Valid @RequestBody CrearReservaRequest request) {
        return reservaService.crear(request, AuthUtil.getCurrentUserId());
    }

    @PreAuthorize("hasAuthority('RESERVA_EDITAR')")
    @PutMapping("/{id}/checkin")
    public Reserva checkIn(@PathVariable Long id) {
        return reservaService.marcarEnCasa(id);
    }

    @PreAuthorize("hasAuthority('RESERVA_EDITAR')")
    @PutMapping("/{id}/checkout")
    public Reserva checkout(@PathVariable Long id) {
        return reservaService.realizarCheckout(id);
    }

    @PreAuthorize("hasAuthority('RESERVA_CANCELAR')")
    @PutMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable Long id) {
        return reservaService.cancelar(id);
    }

}
