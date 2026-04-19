package com.hotelBackend.repository;

import com.hotelBackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // optional<Usuario> es un metodo que puede devolver un usuario o no dependeindo si se encuentra en la DB.
    // findBy significa buscar por el campo que se le indique que en este caso es nombreUsuario.
    Optional<Usuario> findByNombreUsuario(String usuarioNombre);
}
