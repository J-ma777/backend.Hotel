package com.hotelBackend.controller;

import com.hotelBackend.controller.dto.CrearReservaRequest;
import com.hotelBackend.model.Reserva;
import com.hotelBackend.security.util.AuthUtil;
import com.hotelBackend.service.ReservaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@Slf4j
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

    @PreAuthorize("hasAuthority('RESERVA_CHECKIN')")
    @PutMapping("/{id}/checkin")
    public Reserva checkIn(@PathVariable Long id) {
        return reservaService.marcarEnCasa(id);
    }

    @PreAuthorize("hasAuthority('RESERVA_CHECKOUT')")
    @PutMapping("/{id}/checkout")
    public Reserva checkout(@PathVariable Long id) {
        return reservaService.realizarCheckout(id);
    }

    @PreAuthorize("hasAuthority('RESERVA_CANCELAR')")
    @PutMapping("/{id}/cancelar")
    public Reserva cancelar(@PathVariable Long id) {
        return reservaService.cancelar(id);
    }

    @PreAuthorize("hasAuthority('RESERVA_CONFIRMAR')")
    @PutMapping("/{id}/confirmar")
    public Reserva confirmar(@PathVariable Long id, HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullUrl = (query == null || query.isBlank()) ? uri : (uri + "?" + query);

        String authHeader = request.getHeader("Authorization");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication != null ? authentication.getPrincipal() : null;

        Long userId = null;
        try {
            userId = AuthUtil.getCurrentUserId();
        } catch (Exception ignored) {
            // si no hay auth todavía o principal no es CustomUserDetails
        }

        log.info(
                "[RESERVA_CONFIRMAR] {} {} | pathVarId={} (type={}) | AuthorizationHeaderPresent={} | authName={} | principalType={} | extractedUserId={}",
                method,
                fullUrl,
                id,
                (id == null ? "null" : id.getClass().getName()),
                (authHeader != null),
                (authentication != null ? authentication.getName() : null),
                (principal != null ? principal.getClass().getName() : null),
                userId
        );

        log.info(
                "[RESERVA_CONFIRMAR] SecurityContext authorities={}",
                (authentication != null ? authentication.getAuthorities() : null)
        );

        return reservaService.confirmar(id);
    }

}
