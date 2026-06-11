package com.hotelbackend.service;

import com.hotelbackend.model.Rol;

import java.util.List;

public interface RolService {

    List<Rol> obtenerRoles();

    Rol obtenerRolPorId(Long id);

    Rol asignarPermisos(Long rolId, List<Long> permisosIds);

    Rol crearRol(Rol rol);
}
