package com.hotelbackend.service.implementaciones;

import com.hotelbackend.model.Permiso;
import com.hotelbackend.model.Rol;
import com.hotelbackend.repository.PermisoRepository;
import com.hotelbackend.repository.RolRepository;
import com.hotelbackend.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    // Listar todos los roles
    @Override
    public List<Rol> obtenerRoles() {
        return rolRepository.findAll();
    }

    // Obtener roL por ID
    @Override
    public Rol obtenerRolPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    // Asignar un permiso a un rol
    @Override
    public Rol asignarPermisos(Long rolId, List<Long> permisosIds) {

        // 1. Buscar el rol
        Rol rol = rolRepository.findById(rolId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        // 2. Obtener permisos desde la BD
        List<Permiso> permisos = permisoRepository.findAllById(permisosIds);

        // 3. Asignar permisos al rol
        rol.setPermisos(new HashSet<>(permisos));

        // 4. Guardar cambios
        return rolRepository.save(rol);
    }

    @Override
    public Rol crearRol(Rol rol) {
        return rolRepository.save(rol);
    }

}
