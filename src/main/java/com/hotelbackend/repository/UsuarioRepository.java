package com.hotelbackend.repository;

import com.hotelbackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // optional<Usuario> es un metodo que puede devolver un usuario o no dependeindo si se encuentra en la DB.
    // findBy significa buscar por el campo que se le indique que en este caso es nombreUsuario.
    Optional<Usuario> findByNombreUsuario(String usuarioNombre);

    boolean existsByCorreo(String correo);

    @Query("""
    SELECT DISTINCT u FROM Usuario u
    JOIN FETCH u.rol r
    JOIN FETCH r.permisos
    WHERE u.nombreUsuario = :username
""")
    Optional<Usuario> findByNombreUsuarioWithPermisos(@Param("username") String username);

}
