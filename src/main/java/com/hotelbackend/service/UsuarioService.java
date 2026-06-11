package com.hotelbackend.service;

import com.hotelbackend.dto.request.CrearUsuarioRequest;
import com.hotelbackend.model.Usuario;

import java.util.List;

public interface UsuarioService {

    List<Usuario> obtenerUsuarios();

    Usuario obtenerUsuarioPorId(Long id);

    Usuario crearUsuario(CrearUsuarioRequest request);

    Usuario asignarRol(Long usuarioId, Long rolId);

}
