package com.hotelbackend.controller;

import com.hotelbackend.dto.request.CrearTicketRequest;
import com.hotelbackend.model.TicketMantenimiento;
import com.hotelbackend.security.util.AuthUtil;
import com.hotelbackend.service.TicketMantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mantenimiento")
@RequiredArgsConstructor
public class TicketMantenimientoController {

    private final TicketMantenimientoService ticketMantenimientoService;

    @PutMapping("/{ticketId}/resolver")
    @PreAuthorize("hasAuthority('MANTENIMIENTO_GESTIONAR')")
    public ResponseEntity<TicketMantenimiento> resolverTicket(
            @PathVariable Long ticketId
    ) {
        TicketMantenimiento ticket =
                ticketMantenimientoService.resolverTicket(ticketId, AuthUtil.getCurrentUserId());

        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{id}/en-proceso")
    @PreAuthorize("hasAuthority('MANTENIMIENTO_GESTIONAR')")
    public TicketMantenimiento marcarEnProceso(@PathVariable Long id) {
        return ticketMantenimientoService.marcarEnProceso(id);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANTENIMIENTO_VER')")
    public ResponseEntity<List<TicketMantenimiento>> listarTickets() {
        List<TicketMantenimiento> tickets = ticketMantenimientoService.listarTickets();
        return ResponseEntity.ok(tickets);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANTENIMIENTO_CREAR')")
    public ResponseEntity<TicketMantenimiento> crearTicket(@RequestBody CrearTicketRequest request) {

        TicketMantenimiento ticket = ticketMantenimientoService
                .crearManual(request.getHabitacionId(), request.getDescripcion());

        return ResponseEntity.ok(ticket);
    }
}
