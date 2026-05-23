package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.dto.response.HabitacionResponse;
import com.hotelBackend.model.Habitacion;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.ReservaRepository;
import com.hotelBackend.service.HabitacionService;
import com.hotelBackend.service.PlanTarifarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final PlanTarifarioService planTarifarioService;
    private final ReservaRepository reservaRepository;

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
    public List<HabitacionResponse> listar() {
        return habitacionRepository.findAll()
                .stream()
                .map(h -> new HabitacionResponse(
                        h.getId(),
                        h.getNumero(),
                        h.getEstado().name(),
                        h.getPiso(),
                        h.getTipoHabitacion().getNombre()
                ))
                .toList();
    }

    @Override
    public List<HabitacionResponse> buscarDisponibles(LocalDate inicio, LocalDate fin) {

        List<Habitacion> disponibles = habitacionRepository.findDisponibles(inicio, fin); // habitacionRepository.findDisponibles: Método clave

        return disponibles.stream()
                .map(h -> new HabitacionResponse(
                        h.getId(),
                        h.getNumero(),
                        h.getEstado().name(),
                        h.getPiso(),
                        h.getTipoHabitacion().getNombre()
                ))
                .toList();
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
