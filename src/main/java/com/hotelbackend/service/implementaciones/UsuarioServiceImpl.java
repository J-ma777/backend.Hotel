package com.hotelbackend.service.implementaciones;

import com.hotelbackend.dto.request.CrearUsuarioRequest;
import com.hotelbackend.exception.UsuarioDuplicadoException;
import com.hotelbackend.model.Rol;
import com.hotelbackend.model.Usuario;
import com.hotelbackend.repository.RolRepository;
import com.hotelbackend.repository.UsuarioRepository;
import com.hotelbackend.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Usuario> obtenerUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public Usuario crearUsuario(CrearUsuarioRequest request) {

        // Validar usuario duplicado
        if (usuarioRepository.findByNombreUsuario(request.getNombreUsuario()).isPresent()) {
            throw new UsuarioDuplicadoException("El nombre de usuario ya está en uso");
        }

        // Validar correo duplicado
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new UsuarioDuplicadoException("El correo ya está registrado");
        }

        // Validar contraseña
        if (request.getContrasena() == null || request.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }

        // Buscar rol
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        Usuario usuario = new Usuario();

        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setCorreo(request.getCorreo());

        // Encriptación segura
        usuario.setContrasenaHash(
                passwordEncoder.encode(request.getContrasena())
        );

        usuario.setRol(rol);

        // Estado activo por defecto
        usuario.setEstado(true);

        usuario.setCreadoEn(LocalDateTime.now(ZoneId.systemDefault()));
        usuario.setModificadoEn(LocalDateTime.now(ZoneId.systemDefault()));

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario asignarRol(Long usuarioId, Long rolId) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        usuario.setRol(rol);

        return usuarioRepository.save(usuario);
    }

}
