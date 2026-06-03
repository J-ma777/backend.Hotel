package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.dto.response.HabitacionResponse;
import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoReserva;
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

    // LISTAR (CORREGIDO - ahora usa ocupación real)
    @Override
    public List<HabitacionResponse> listar() {
        return habitacionRepository.findAll()
                .stream()
                .map(h -> {
                    boolean ocupada = reservaRepository.existsByHabitacionIdAndEstadoIn(
                            h.getId(),
                            List.of(
                                    EstadoReserva.CONFIRMADA,
                                    EstadoReserva.EN_CASA
                            )
                    );

                    return new HabitacionResponse(h, ocupada);
                })
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

    @Override
    public List<Habitacion> obtenerParaMantenimiento() {

        return habitacionRepository.findAll()
                .stream()
                .filter(h -> {

                    // excluir fuera de servicio
                    if (h.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
                        return false;
                    }

                    // excluir ocupadas (clave)
                    boolean ocupada = reservaRepository.existsByHabitacionIdAndEstadoIn(
                            h.getId(),
                            List.of(
                                    EstadoReserva.CONFIRMADA,
                                    EstadoReserva.EN_CASA
                            )
                    );

                    return !ocupada;
                })
                .toList();
    }

    @Override
    public List<HabitacionResponse> obtenerDisponiblesPorTipo(
            Long tipoHabitacionId,
            LocalDate inicio,
            LocalDate fin) {

        return habitacionRepository
                .findDisponiblesPorTipo(tipoHabitacionId, inicio, fin)
                .stream()
                .map(HabitacionResponse::new)
                .toList();
    }

}
