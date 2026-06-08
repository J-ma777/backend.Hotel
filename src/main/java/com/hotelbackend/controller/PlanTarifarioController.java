package com.hotelbackend.controller;

import com.hotelbackend.dto.request.PlanTarifarioRequest;
import com.hotelbackend.dto.response.PlanTarifarioResponse;
import com.hotelbackend.service.PlanTarifarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/plan-tarifarios")
public class PlanTarifarioController {

    private final PlanTarifarioService service;

    public PlanTarifarioController(PlanTarifarioService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TARIFAS_GESTIONAR')")
    public PlanTarifarioResponse crear(@RequestBody PlanTarifarioRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TARIFAS_GESTIONAR')")
    public PlanTarifarioResponse actualizar(
            @PathVariable Long id,
            @RequestBody PlanTarifarioRequest request) {
        return service.actualizar(id, request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TARIFAS_VER')")
    public List<PlanTarifarioResponse> listarTodos() {
        return service.listarTodos();
    }

    @GetMapping("/tipo/{tipoHabitacionId}")
    public List<PlanTarifarioResponse> listarPorTipo(@PathVariable Long tipoHabitacionId) {
        return service.listarPorTipo(tipoHabitacionId);
    }
}