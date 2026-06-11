package com.hotelbackend.controller;

import com.hotelbackend.model.Rol;
import com.hotelbackend.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    //Listar todos los roles
    @GetMapping
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<List<Rol>> obtenerRoles() {
        return ResponseEntity.ok(rolService.obtenerRoles());
    }

    //Obtener un rol por ID (Para poder editarlo)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Rol> obtenerRolPorId(@PathVariable Long id) {
        Rol rol = rolService.obtenerRolPorId(id);
        return ResponseEntity.ok(rol);
    }

    // Asignar permisos a un rol
    @PutMapping("/{id}/permisos")
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Rol> asignarPermisos(
            @PathVariable Long id,
            @RequestBody List<Long> permisosIds) {

        Rol rolActualizado = rolService.asignarPermisos(id, permisosIds);
        return ResponseEntity.ok(rolActualizado);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Rol> crearRol(@RequestBody Rol rol) {
        return ResponseEntity.ok(rolService.crearRol(rol));
    }

}
