package com.hotelBackend.controller;

import com.hotelBackend.model.PlanTarifario;
import com.hotelBackend.service.PlanTarifarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tarifas")
public class PlanTarifarioController {

    private final PlanTarifarioService service;

    public PlanTarifarioController(PlanTarifarioService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TARIFAS_GESTIONAR')")
    public PlanTarifario crear(@RequestBody PlanTarifario plan) {
        return service.crear(plan);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TARIFAS_GESTIONAR')")
    public PlanTarifario actualizar(
            @PathVariable Long id,
            @RequestBody PlanTarifario plan) {
        return service.actualizar(id, plan);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TARIFAS_VER')")
    public List<PlanTarifario> listarTodos() {
        return service.listarTodos();
    }

}
