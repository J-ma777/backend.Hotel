package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.model.TipoHabitacion;
import com.hotelBackend.repository.TipoHabitacionRepository;
import com.hotelBackend.service.TipoHabitacionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional
public class TipoHabitacionServiceImpl implements TipoHabitacionService {


    private final TipoHabitacionRepository tipoHabitacionRepository;

    @Override
    public TipoHabitacion guardar(TipoHabitacion tipoHabitacion) {
        return tipoHabitacionRepository.save(tipoHabitacion);
    }

    @Override
    public List<TipoHabitacion> listar() {
        return tipoHabitacionRepository.findAll();
    }

}
