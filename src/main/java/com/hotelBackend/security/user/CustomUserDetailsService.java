package com.hotelBackend.security.user;

import com.hotelBackend.model.Usuario;
import com.hotelBackend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String usuarioNombre)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByNombreUsuario(usuarioNombre)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado con el nombre: " + usuarioNombre
                        )
                );

        return new CustomUserDetails(usuario);
    }
}
