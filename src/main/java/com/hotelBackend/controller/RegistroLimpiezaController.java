package com.hotelBackend.controller;

import com.hotelBackend.model.RegistroLimpieza;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.service.RegistroLimpiezaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/limpieza")
@RequiredArgsConstructor
public class RegistroLimpiezaController {

    private final RegistroLimpiezaService registroLimpiezaService;

    @PostMapping("/habitacion/{habitacionId}")
    @PreAuthorize("hasAuthority('REGISTRAR_LIMPIEZA')")
    public ResponseEntity<RegistroLimpieza> registrarCambioEstado(
            @PathVariable Long habitacionId,
            @RequestParam EstadoHabitacion estadoNuevo,
            @RequestParam(required = false) String notas,
            @RequestParam Long usuarioId
    ) {
        return ResponseEntity.ok(
                registroLimpiezaService.registrarCambioEstado(
                        habitacionId,
                        estadoNuevo,
                        notas,
                        usuarioId
                )
        );
    }

    @GetMapping("/habitacion/{habitacionId}")
    @PreAuthorize("hasAuthority('VER_LIMPIEZAS')")
    public ResponseEntity<List<RegistroLimpieza>> listarPorHabitacion(
            @PathVariable Long habitacionId
    ) {
        return ResponseEntity.ok(
                registroLimpiezaService.listarPorHabitacion(habitacionId)
        );
    }

}
