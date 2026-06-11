package com.hotelbackend.controller;


import com.hotelbackend.dto.request.CrearUsuarioRequest;
import com.hotelbackend.model.Usuario;
import com.hotelbackend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    //Listar Usuarios
    @GetMapping
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')") // temporal
    public ResponseEntity<List<Usuario>> obtenerUsuarios() {
        return ResponseEntity.ok(usuarioService.obtenerUsuarios());
    }

    //Obtner por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    //Crear Usuario
    @PostMapping
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody CrearUsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.crearUsuario(request));
    }

    //Asiganr rol a usuario
    @PutMapping("/{id}/rol/{rolId}")
    @PreAuthorize("hasAuthority('GESTIONAR_USUARIOS')")
    public ResponseEntity<Usuario> asignarRol(
            @PathVariable Long id,
            @PathVariable Long rolId) {

        return ResponseEntity.ok(usuarioService.asignarRol(id, rolId));
    }

}
