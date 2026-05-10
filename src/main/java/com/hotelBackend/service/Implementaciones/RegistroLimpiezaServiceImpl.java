package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.RegistroLimpieza;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoTicket;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.RegistroLimpiezaRepository;
import com.hotelBackend.repository.TicketMantenimientoRepository;
import com.hotelBackend.service.RegistroLimpiezaService;
import com.hotelBackend.service.TicketMantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistroLimpiezaServiceImpl implements RegistroLimpiezaService {

    // Inyección de dependencias de los repositorios y servicios necesarios
    private final RegistroLimpiezaRepository registroLimpiezaRepository;
    private final HabitacionRepository habitacionRepository;
    private final TicketMantenimientoService ticketMantenimientoService;
    private final TicketMantenimientoRepository ticketMantenimientoRepository;

    @Override
    public RegistroLimpieza registrarCambioEstado(
            Long habitacionId,
            EstadoHabitacion estadoNuevo,
            String notas,
            Long usuarioId
    ) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        EstadoHabitacion estadoAnterior = habitacion.getEstado();

        if (estadoAnterior == estadoNuevo) {
            throw new IllegalStateException("El estado nuevo no puede ser igual al estado actual");
        }

        RegistroLimpieza registro = new RegistroLimpieza();
        registro.setHabitacion(habitacion);
        registro.setEstadoAnterior(estadoAnterior);
        registro.setEstadoNuevo(estadoNuevo);
        registro.setNotas(notas);
        registro.setCambiadoEn(LocalDateTime.now());
        registro.setCambiadoPor(usuarioId);

        // Actualizar el estado de la habitación
        habitacion.setEstado(estadoNuevo);

        // Regla PMS: Genrar ticket si hay incidencia en la limpieza
        if (
                estadoNuevo == EstadoHabitacion.FUERA_DE_SERVICIO ||
                        (estadoNuevo == EstadoHabitacion.INSPECCIONADA &&
                                notas != null && !notas.isBlank())
        ) {
            ticketMantenimientoService.crearDesdeLimpieza(
                    habitacion,
                    notas,
                    usuarioId
            );
        }

        // Regla: INSPECCIONADA -> DISPONIBLE
        // Solo si no existen thickets abiertos para esa habitación
        if (estadoNuevo == EstadoHabitacion.INSPECCIONADA) {
            boolean tieneTicketsAbiertos =
                    ticketMantenimientoRepository
                            .existsByHabitacionIdAndEstado(
                                    habitacion.getId(),
                                    EstadoTicket.ABIERTO
                            );

            if (!tieneTicketsAbiertos) {
                habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
            }
        }

        habitacionRepository.save(habitacion);
        return registroLimpiezaRepository.save(registro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroLimpieza> listarPorHabitacion(Long habitacionId) {
        return registroLimpiezaRepository.findByHabitacionId(habitacionId);
    }
}

