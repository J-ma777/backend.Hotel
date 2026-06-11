package com.hotelbackend.controller;

import com.hotelbackend.model.Permiso;
import com.hotelbackend.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final PermisoService permisoService;

    // Listar todos los permisos
    @GetMapping
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<List<Permiso>> obtenerPermisos() {
        List<Permiso> permisos = permisoService.obtenerPermisos();
        return ResponseEntity.ok(permisos);
    }

}
