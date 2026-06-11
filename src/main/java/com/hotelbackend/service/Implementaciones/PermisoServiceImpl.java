package com.hotelbackend.service.Implementaciones;

import com.hotelbackend.model.Permiso;
import com.hotelbackend.repository.PermisoRepository;
import com.hotelbackend.service.PermisoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermisoServiceImpl implements PermisoService {

    private final PermisoRepository permisoRepository;

    public PermisoServiceImpl(PermisoRepository permisoRepository) {
        this.permisoRepository = permisoRepository;
    }

    @Override
    public List<Permiso> obtenerPermisos() {
        return permisoRepository.findAll();
    }

}
