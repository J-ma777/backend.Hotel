package com.hotelbackend.service.implementaciones;

import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.RegistroLimpieza;
import com.hotelbackend.model.enums.EstadoHabitacion;
import com.hotelbackend.model.enums.EstadoReserva;
import com.hotelbackend.model.enums.EstadoTicket;
import com.hotelbackend.repository.HabitacionRepository;
import com.hotelbackend.repository.RegistroLimpiezaRepository;
import com.hotelbackend.repository.ReservaRepository;
import com.hotelbackend.repository.TicketMantenimientoRepository;
import com.hotelbackend.service.RegistroLimpiezaService;
import com.hotelbackend.service.TicketMantenimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final ReservaRepository reservaRepository;

    @Override
    public RegistroLimpieza registrarCambioEstado(
            Long habitacionId,
            EstadoHabitacion estadoNuevo,
            String notas,
            Long usuarioId
    ) {
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));


        // AQUÍ VA LA VALIDACIÓN DE QUE LA HABITACIÓN NO ESTÉ OCUPADA ANTES DE PERMITIR CAMBIOS DE LIMPIEZA
        boolean ocupada = reservaRepository.existsByHabitacionIdAndEstadoIn(
                habitacion.getId(),
                List.of(
                        EstadoReserva.CONFIRMADA,
                        EstadoReserva.EN_CASA
                )
        );

        if (ocupada) {
            throw new IllegalStateException(
                    "No se puede modificar limpieza de una habitación ocupada"
            );
        }

        EstadoHabitacion estadoAnterior = habitacion.getEstado();

        if (estadoAnterior == estadoNuevo) {
            throw new IllegalStateException("El estado nuevo no puede ser igual al estado actual");
        }

        RegistroLimpieza registro = new RegistroLimpieza();
        registro.setHabitacion(habitacion);
        registro.setEstadoAnterior(estadoAnterior);
        registro.setEstadoNuevo(estadoNuevo);
        registro.setNotas(notas);
        registro.setCambiadoEn(LocalDateTime.now(ZoneId.systemDefault()));
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
                            .existsByHabitacionIdAndEstadoIn(
                                    habitacion.getId(),
                                    List.of(EstadoTicket.ABIERTO, EstadoTicket.EN_PROCESO)
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

