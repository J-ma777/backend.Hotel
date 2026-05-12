package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.controller.dto.CrearReservaRequest;
import com.hotelBackend.exception.ReservaNoEncontradaException;
import com.hotelBackend.model.Habitacion;
import com.hotelBackend.model.Reserva;
import com.hotelBackend.model.TipoHabitacion;
import com.hotelBackend.model.enums.EstadoHabitacion;
import com.hotelBackend.model.enums.EstadoReserva;
import com.hotelBackend.model.enums.TipoTransaccion;
import com.hotelBackend.repository.HabitacionRepository;
import com.hotelBackend.repository.ReservaRepository;
import com.hotelBackend.service.PlanTarifarioService;
import com.hotelBackend.service.ReservaService;
import com.hotelBackend.service.TransaccionFolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final PlanTarifarioService planTarifarioService;
    private final TransaccionFolioService transaccionFolioService;

    // Metodo
    @Override
    public Reserva crear(CrearReservaRequest request) {

        Reserva reserva = new Reserva();

        reserva.setFechaEntrada(request.getFechaEntrada());
        reserva.setFechaSalida(request.getFechaSalida());
        reserva.setCantidadHuespedes(request.getCantidadHuespedes());
        reserva.setNombreHuesped(request.getNombreHuesped());
        reserva.setDocumentoHuesped(request.getDocumentoHuesped());

        // Regla PMS básica (NO inventada)
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setCreadoEn(LocalDateTime.now());
        reserva.setCreadoPor(1L);

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
    public Reserva marcarEnCasa(Long id) { // Aca es el famoso check-in
        Reserva reserva = obtenerPorId(id);

        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException(
                    "Solo se puede hacer check-in a una reserva CONFIRMADA"
            );
        }

        Habitacion habitacion = reserva.getHabitacion();

        if (habitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new IllegalStateException(
                    "No se puede hacer check-in a una habitación fuera de servicio"
            );
        }

        // HU-20: Generar cargos por noche
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
                    null   // registradoPor: usa el mismo criterio que ya manejas
            );
        }
        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepository.save(habitacion);

        reserva.setEstado(EstadoReserva.EN_CASA);
        return reservaRepository.save(reserva);
    }

    @Override
    public Reserva realizarCheckout(Long id) {

        Reserva reserva = obtenerPorId(id);

        if (reserva.getEstado() != EstadoReserva.EN_CASA) {
            throw new IllegalStateException(
                    "Solo se puede hacer checkout a una reserva EN_CASA"
            );
        }

        // Liberar habitación
        Habitacion habitacion = reserva.getHabitacion();
        habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
        habitacionRepository.save(habitacion);

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