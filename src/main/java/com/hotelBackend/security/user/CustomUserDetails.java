package com.hotelBackend.security.user;

import com.hotelBackend.model.Permiso;
import com.hotelBackend.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Permisos del rol
        for (Permiso permiso : usuario.getRol().getPermisos()) {
            authorities.add(
                    new SimpleGrantedAuthority(permiso.getNombre())
            );
        }

        // Rol (opcional, pero recomendable)
        authorities.add(
                new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre())
        );

        return authorities;
    }

    @Override
    public String getPassword() {
        return usuario.getContrasenaHash();
    }

    @Override
    public String getUsername() {
        return usuario.getNombreUsuario();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.getEstado();
    }

    // Helpers útiles
    public Long getId() {
        return usuario.getId();
    }

    public String getRolNombre() {
        return usuario.getRol().getNombre();
    }
}