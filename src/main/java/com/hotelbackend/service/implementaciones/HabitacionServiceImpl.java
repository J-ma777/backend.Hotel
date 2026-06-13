package com.hotelbackend.service.implementaciones;

import com.hotelbackend.dto.request.ActualizarHabitacionRequest;
import com.hotelbackend.dto.response.HabitacionMapaResponse;
import com.hotelbackend.dto.response.HabitacionResponse;
import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.Reserva;
import com.hotelbackend.model.TipoHabitacion;
import com.hotelbackend.model.enums.EstadoHabitacion;
import com.hotelbackend.model.enums.EstadoReserva;
import com.hotelbackend.repository.HabitacionRepository;
import com.hotelbackend.repository.ReservaRepository;
import com.hotelbackend.repository.TipoHabitacionRepository;
import com.hotelbackend.service.HabitacionService;
import com.hotelbackend.service.PlanTarifarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class HabitacionServiceImpl implements HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final PlanTarifarioService planTarifarioService;
    private final ReservaRepository reservaRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;

    @Override
    public Habitacion guardar(Habitacion habitacion) {
        // Asignar estado por defecto si no viene
        if (habitacion.getEstado() == null){
            habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        }
        return habitacionRepository.save(habitacion);
    }

    @Override
    public HabitacionResponse actualizar(Long id, ActualizarHabitacionRequest request) {

        Habitacion actual = obtenerPorId(id);

        if (actual.getEstado() == EstadoHabitacion.OCUPADA) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede editar una habitación ocupada");
        }


        if (habitacionRepository.existsByNumeroAndIdNot(request.getNumero(), id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una habitación con ese número"
            );
        }

        actual.setNumero(request.getNumero());
        actual.setPiso(request.getPiso());

        Long tipoId = request.getTipoHabitacionId();

        TipoHabitacion tipo = tipoHabitacionRepository.findById(tipoId)
                .orElseThrow(() -> new RuntimeException("Tipo de habitación no encontrado"));

        actual.setTipoHabitacion(tipo);

        Habitacion guardada = habitacionRepository.save(actual);

        return new HabitacionResponse(guardada);
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

        Habitacion habitacion = obtenerPorId(id);

        // No se puede eliminar si está ocupada
        if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
            throw new RuntimeException("No se puede eliminar una habitación ocupada");
        }

        // No se puede eliminar si tiene reservas activas
        boolean tieneReservas = reservaRepository.existsByHabitacionIdAndEstadoIn(
                id,
                List.of(
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.EN_CASA
                )
        );

        if (tieneReservas) {
            throw new RuntimeException("No se puede eliminar la habitación porque tiene reservas activas");
        }

        habitacionRepository.delete(habitacion);
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
            LocalDate fin
    ) {
        return habitacionRepository
                .findDisponiblesPorTipo(
                        tipoHabitacionId,
                        inicio,
                        fin,
                        EstadoHabitacion.DISPONIBLE,
                        List.of(
                                EstadoReserva.CONFIRMADA,
                                EstadoReserva.EN_CASA
                        )
                )
                .stream()
                .map(HabitacionResponse::new)
                .toList();

    }

    @Override
    public Habitacion cambiarEstado(Long id, EstadoHabitacion nuevoEstado) {

        Habitacion habitacion = obtenerPorId(id);
        EstadoHabitacion actual = habitacion.getEstado();

        // FUERA DE SERVICIO (entra desde cualquier estado)
        if (nuevoEstado == EstadoHabitacion.FUERA_DE_SERVICIO) {
            habitacion.setEstado(nuevoEstado);
            return habitacionRepository.save(habitacion);
        }

        // Salir de FUERA_DE_SERVICIO solo a DISPONIBLE
        if (actual == EstadoHabitacion.FUERA_DE_SERVICIO) {
            if (nuevoEstado != EstadoHabitacion.DISPONIBLE) {
                throw new RuntimeException("Solo se puede pasar de FUERA_DE_SERVICIO a DISPONIBLE");
            }
            habitacion.setEstado(nuevoEstado);
            return habitacionRepository.save(habitacion);
        }

        // Flujo normal (RF-10)
        boolean valido = switch (actual) {
            case DISPONIBLE -> nuevoEstado == EstadoHabitacion.OCUPADA;
            case OCUPADA -> nuevoEstado == EstadoHabitacion.SUCIA;
            case SUCIA -> nuevoEstado == EstadoHabitacion.LIMPIANDO;
            case LIMPIANDO -> nuevoEstado == EstadoHabitacion.INSPECCIONADA;
            case INSPECCIONADA -> nuevoEstado == EstadoHabitacion.DISPONIBLE;
            default -> false;
        };

        if (!valido) {
            throw new RuntimeException("Transición de estado no válida: " + actual + " → " + nuevoEstado);
        }

        habitacion.setEstado(nuevoEstado);
        return habitacionRepository.save(habitacion);
    }

    @Override
    public List<HabitacionMapaResponse> obtenerMapa() {

        List<Habitacion> habitaciones = habitacionRepository.findAll();

        return habitaciones.stream().map(h -> {

            HabitacionMapaResponse dto = new HabitacionMapaResponse();

            dto.setId(h.getId());
            dto.setNumero(h.getNumero());
            dto.setEstado(h.getEstado().name());
            dto.setPiso(h.getPiso());
            dto.setTipoNombre(h.getTipoHabitacion().getNombre());
            dto.setCapacidad(h.getTipoHabitacion().getCapacidad());
            dto.setTipoId(h.getTipoHabitacion().getId());

            // buscar reserva activa
            Optional<Reserva> reservaOpt = reservaRepository
                    .findByHabitacionAndEstado(h, EstadoReserva.EN_CASA);

            if (reservaOpt.isPresent()) {
                Reserva r = reservaOpt.get();

                dto.setNombreHuesped(r.getNombreHuesped());
                dto.setCantidadHuespedes(r.getCantidadHuespedes());
                dto.setFechaSalida(r.getFechaSalida());
                dto.setReservaId(r.getId());
            }

            return dto;

        }).toList();
    }

}
