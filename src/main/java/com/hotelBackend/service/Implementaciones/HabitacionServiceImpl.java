package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.model.Habitacion;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.service.HabitacionService;
import com.hotelBackend.service.PlanTarifarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final PlanTarifarioService planTarifarioService;

    @Override
    public Habitacion guardar(Habitacion habitacion) {
        return habitacionRepository.save(habitacion);
    }

    @Override
    public Habitacion actualizar(Long id, Habitacion habitacion) {
        Habitacion actual = obtenerPorId(id);

        actual.setNumero(habitacion.getNumero());
        actual.setPiso(habitacion.getPiso());
        actual.setEstado(habitacion.getEstado());
        actual.setTipoHabitacion(habitacion.getTipoHabitacion());

        return habitacionRepository.save(actual);
    }


    @Override
    public List<Habitacion> listar() {
        return habitacionRepository.findAll();
    }

    @Override
    public Habitacion obtenerPorId(Long id) {
        return habitacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
    }

    @Override
    public void eliminar(Long id) {
        habitacionRepository.deleteById(id);
    }

}
