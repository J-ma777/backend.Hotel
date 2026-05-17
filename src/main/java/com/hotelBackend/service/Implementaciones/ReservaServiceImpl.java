package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.controller.dto.CrearReservaRequest;
import com.hotelBackend.exception.ReservaNoEncontradaException;
import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.Reserva;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoReserva;
import com.hotelBackend.model.enums.TipoTransaccion;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.ReservaRepository;
import com.hotelBackend.security.util.AuthUtil;
import com.hotelBackend.service.PlanTarifarioService;
import com.hotelBackend.service.ReservaService;
import com.hotelBackend.service.TransaccionFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final PlanTarifarioService planTarifarioService;
    private final TransaccionFolioService transaccionFolioService;

    // Metodo para crear una nueva reserva
    @Override
    public Reserva crear(CrearReservaRequest request, Long userId) {

        Reserva reserva = new Reserva();

        reserva.setFechaEntrada(request.getFechaEntrada());
        reserva.setFechaSalida(request.getFechaSalida());
        reserva.setCantidadHuespedes(request.getCantidadHuespedes());
        reserva.setNombreHuesped(request.getNombreHuesped());
        reserva.setDocumentoHuesped(request.getDocumentoHuesped());

        // Regla PMS básica (NO inventada)
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setCreadoEn(LocalDateTime.now());
        reserva.setCreadoPor(userId);

        /*// TipoHabitacion se resuelve aquí (repositorio)
        TipoHabitacion tipoHabitacion = tipoHabitacionRepository
                .findById(request.getTipoHabitacionId())
                .orElseThrow(() -> new RuntimeException("Tipo de habitación no encontrado"));

        reserva.setTipoHabitacion(tipoHabitacion);*/

        return reservaRepository.save(reserva);
    }

    // Meétodo para listar todas las reservas, útil para el HU-19: Listar reservas
    @Override
    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    @Override
    public Reserva obtenerPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() ->
                        new ReservaNoEncontradaException(id)
                );
    }

    @Override
    public Reserva cancelar(Long id) {
        Reserva reserva = obtenerPorId(id);

        if (reserva.getEstado() == EstadoReserva.SALIDA_CHECKOUT) {
            throw new IllegalStateException(
                    "No se puede cancelar una reserva finalizada"
            );
        }

        Habitacion habitacion = reserva.getHabitacion();
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacionRepository.save(habitacion);

        reserva.setEstado(EstadoReserva.CANCELADA);
        return reservaRepository.save(reserva);
    }

    @Override
    public Reserva marcarEnCasa(Long id) { // CHECK-IN
        // Validar reserva existe
        Reserva reserva = obtenerPorId(id);

        // Validar estado de reserva
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new com.hotelBackend.exception.EstadoReservaInvalidoException(
                    "Solo se puede hacer check-in a una reserva CONFIRMADA. Estado actual: " + reserva.getEstado()
            );
        }

        // Validar fechas
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(reserva.getFechaEntrada())) {
            throw new com.hotelBackend.exception.ValidacionFechasException(
                    "No se puede hacer check-in antes de la fecha de entrada (" + reserva.getFechaEntrada() + ")"
            );
        }

        if (hoy.isAfter(reserva.getFechaSalida())) {
            throw new com.hotelBackend.exception.ValidacionFechasException(
                    "La reserva ya ha vencido (" + reserva.getFechaSalida() + ")"
            );
        }

        // Validar habitación
        Habitacion habitacion = reserva.getHabitacion();
        if (habitacion == null) {
            throw new com.hotelBackend.exception.HabitacionNoDisponibleException(
                    "La reserva no tiene habitación asignada"
            );
        }

        if (habitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new com.hotelBackend.exception.HabitacionNoDisponibleException(
                    "La habitación está fuera de servicio"
            );
        }

        if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
            throw new com.hotelBackend.exception.HabitacionNoDisponibleException(
                    "La habitación ya está ocupada"
            );
        }

        // Validar capacidad
        if (reserva.getCantidadHuespedes() > habitacion.getTipoHabitacion().getCapacidad()) {
            throw new com.hotelBackend.exception.ValidacionFechasException(
                    "Cantidad de huéspedes (" + reserva.getCantidadHuespedes() +
                    ") supera capacidad de habitación (" + habitacion.getTipoHabitacion().getCapacidad() + ")"
            );
        }

        Long registradoPor;
        try {
            registradoPor = AuthUtil.getCurrentUserId();
        } catch (IllegalStateException e) {
            // Fallback seguro (por ejemplo, ejecución batch o unit tests sin SecurityContext)
            registradoPor = reserva.getCreadoPor();
        }

        // Generar cargos por noche
        for (var noche = reserva.getFechaEntrada();
             noche.isBefore(reserva.getFechaSalida());
             noche = noche.plusDays(1)) {

            var tarifa = planTarifarioService.obtenerTarifaParaNoche(
                    habitacion.getTipoHabitacion().getId(),
                    noche
            );

            transaccionFolioService.registrarTransaccion(
                    reserva.getId(),
                    TipoTransaccion.CARGO_NOCHE,
                    "Noche " + noche + " - " + tarifa.getNombre(),
                    tarifa.getPrecioPorNoche(),
                    1,
                    registradoPor
            );
        }

        // Marcar habitación como ocupada
        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepository.save(habitacion);

        // Cambiar estado de reserva a EN_CASA
        reserva.setEstado(EstadoReserva.EN_CASA);
        return reservaRepository.save(reserva);
    }

    @Override
    public Reserva realizarCheckout(Long id) {
        // Validar reserva existe
        Reserva reserva = obtenerPorId(id);

        // Validar estado de reserva
        if (reserva.getEstado() != EstadoReserva.EN_CASA) {
            throw new com.hotelBackend.exception.EstadoReservaInvalidoException(
                    "Solo se puede hacer checkout a una reserva EN_CASA. Estado actual: " + reserva.getEstado()
            );
        }

        // Validar habitación existe
        Habitacion habitacion = reserva.getHabitacion();
        if (habitacion == null) {
            throw new com.hotelBackend.exception.HabitacionNoDisponibleException(
                    "La reserva no tiene habitación asignada"
            );
        }

        // Liberar habitación (marcar como disponible)
        // Nota: El estado SUCIA/LIMPIEZA es responsabilidad de RegistroLimpiezaService
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacionRepository.save(habitacion);

        // Cambiar estado de reserva a SALIDA_CHECKOUT
        reserva.setEstado(EstadoReserva.SALIDA_CHECKOUT);
        return reservaRepository.save(reserva);
    }

    // MÉTODO PARA MARCAR UNA RESERVA COMO NO_PRESENTADA
    @Override
    public void procesarNoPresentadas() {

        LocalDate hoy = LocalDate.now();

        List<Reserva> reservas = reservaRepository
                .findByEstadoAndFechaEntradaBefore(
                        EstadoReserva.CONFIRMADA,
                        hoy
                );

        for (Reserva reserva : reservas) {

            Habitacion habitacion = reserva.getHabitacion();
            habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
            habitacionRepository.save(habitacion);

            reserva.setEstado(EstadoReserva.NO_PRESENTADA);
            reservaRepository.save(reserva);
        }
    }
}
