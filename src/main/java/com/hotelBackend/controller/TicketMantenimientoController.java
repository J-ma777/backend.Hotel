package com.hotelBackend.controller;

import com.hotelBackend.model.TicketMantenimiento;
import com.hotelBackend.security.util.AuthUtil;
import com.hotelBackend.service.TicketMantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mantenimiento")
@RequiredArgsConstructor
public class TicketMantenimientoController {

    private final TicketMantenimientoService ticketMantenimientoService;

    @PutMapping("/{ticketId}/resolver")
    @PreAuthorize("hasAuthority('RESOLVER_TICKET')")
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
}
