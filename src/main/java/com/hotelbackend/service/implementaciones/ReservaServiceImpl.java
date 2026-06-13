package com.hotelbackend.service.implementaciones;

import com.hotelbackend.dto.request.CrearReservaRequest;
import com.hotelbackend.dto.response.DashboardResponse;
import com.hotelbackend.dto.response.FolioResumenResponse;
import com.hotelbackend.dto.response.ReservaResponse;
import com.hotelbackend.exception.EstadoReservaInvalidoException;
import com.hotelbackend.exception.HabitacionNoDisponibleException;
import com.hotelbackend.exception.ReservaNoEncontradaException;
import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.PlanTarifario;
import com.hotelbackend.model.Reserva;
import com.hotelbackend.model.enums.EstadoHabitacion;
import com.hotelbackend.model.enums.EstadoReserva;
import com.hotelbackend.model.enums.TipoTransaccion;
import com.hotelbackend.repository.HabitacionRepository;
import com.hotelbackend.repository.PlanTarifarioRepository;
import com.hotelbackend.repository.ReservaRepository;
import com.hotelbackend.security.util.AuthUtil;
import com.hotelbackend.service.PlanTarifarioService;
import com.hotelbackend.service.ReservaService;
import com.hotelbackend.service.TransaccionFolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;
    private final HabitacionRepository habitacionRepository;
    private final PlanTarifarioService planTarifarioService;
    private final PlanTarifarioRepository planTarifarioRepository;
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
        // Estado inicial del flujo
        // PENDIENTE -> (acción futura) CONFIRMADA -> EN_CASA -> SALIDA_CHECKOUT
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setCreadoEn(LocalDateTime.now(ZoneId.systemDefault()));
        reserva.setCreadoPor(userId);

        // PlanTarifario se resuelve aquí (repositorio)
        PlanTarifario plan = planTarifarioRepository.findById(request.getPlanTarifarioId())
                .orElseThrow(() -> new RuntimeException("Plan tarifario no encontrado"));

        reserva.setPlanTarifario(plan);

        return reservaRepository.save(reserva);
    }

    // Meétodo para listar todas las reservas, útil para el HU-19: Listar reservas
    @Override
    public List<Reserva> listar() {
        return reservaRepository.findAll();
    }

    @Override
    public ReservaResponse obtenerPorId(Long id) {

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));

        return mapToResponse(reserva);
    }


    @Override

    public Reserva cancelar(Long id) {
        // Buscar reserva (404 si no existe)
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));

        // SOLO permitir cancelar si está PENDIENTE o CONFIRMADA
        if (reserva.getEstado() != EstadoReserva.PENDIENTE &&
                reserva.getEstado() != EstadoReserva.CONFIRMADA) {

            throw new EstadoReservaInvalidoException(
                    "Solo se pueden cancelar reservas en estado PENDIENTE o CONFIRMADA. Estado actual: "
                            + reserva.getEstado()
            );
        }

        // Liberar habitación SOLO si existe
        Habitacion habitacion = reserva.getHabitacion();
        if (habitacion != null) {
            habitacion.setEstado(EstadoHabitacion.DISPONIBLE);
            habitacionRepository.save(habitacion);
        }

        // Cambiar estado de la reserva
        reserva.setEstado(EstadoReserva.CANCELADA);

        return reservaRepository.save(reserva);
    }

    private Reserva buscarEntidad(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));
    }

    @Override
    public Reserva marcarEnCasa(Long id) { // CHECK-IN
        // Validar reserva existe
        Reserva reserva = buscarEntidad(id);

        // Validar estado de reserva
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new com.hotelbackend.exception.EstadoReservaInvalidoException(
                    "Solo se puede hacer check-in a una reserva CONFIRMADA. Estado actual: " + reserva.getEstado()
            );
        }

        // Validar fechas
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        if (hoy.isBefore(reserva.getFechaEntrada())) {
            throw new com.hotelbackend.exception.ValidacionFechasException(
                    "No se puede hacer check-in antes de la fecha de entrada (" + reserva.getFechaEntrada() + ")"
            );
        }

        if (hoy.isAfter(reserva.getFechaSalida())) {
            throw new com.hotelbackend.exception.ValidacionFechasException(
                    "La reserva ya ha vencido (" + reserva.getFechaSalida() + ")"
            );
        }

        // Validar habitación
        Habitacion habitacion = reserva.getHabitacion();
        if (habitacion == null) {
            throw new com.hotelbackend.exception.HabitacionNoDisponibleException(
                    "La reserva no tiene habitación asignada"
            );
        }

        if (habitacion.getEstado() == EstadoHabitacion.FUERA_DE_SERVICIO) {
            throw new com.hotelbackend.exception.HabitacionNoDisponibleException(
                    "La habitación está fuera de servicio"
            );
        }

        if (habitacion.getEstado() == EstadoHabitacion.OCUPADA) {
            throw new com.hotelbackend.exception.HabitacionNoDisponibleException(
                    "La habitación ya está ocupada"
            );
        }

        // Validar capacidad
        if (reserva.getCantidadHuespedes() > habitacion.getTipoHabitacion().getCapacidad()) {
            throw new com.hotelbackend.exception.ValidacionFechasException(
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
    public Reserva realizarcheckIn(Long reservaId, Long habitacionId) {

        // 1. Buscar reserva
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // 2. Validar estado
        if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            throw new RuntimeException("Solo reservas CONFIRMADAS pueden hacer check-in");
        }

        // 3. Obtener habitación
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        // 4. Validar disponibilidad física
        if (habitacion.getEstado() != EstadoHabitacion.DISPONIBLE) {
            throw new RuntimeException("La habitación no está disponible");
        }

        // 5. Asignar habitación a la reserva
        reserva.setHabitacion(habitacion);

        // 6. Obtener usuario
        Long userId = AuthUtil.getCurrentUserId();

        // 7. GENERAR CARGOS (motor RF‑07 real)
        for (LocalDate noche = reserva.getFechaEntrada();
             noche.isBefore(reserva.getFechaSalida());
             noche = noche.plusDays(1)) {

            PlanTarifario tarifa = planTarifarioService.obtenerTarifaParaNoche(
                    habitacion.getTipoHabitacion().getId(),
                    noche
            );

            transaccionFolioService.registrarTransaccion(
                    reserva.getId(),
                    TipoTransaccion.CARGO_NOCHE,
                    "Noche " + noche + " - " + tarifa.getNombre(),
                    tarifa.getPrecioPorNoche(),
                    1,
                    userId
            );
        }

        // 8. Cambiar estado habitación (OCUPADA)
        habitacion.setEstado(EstadoHabitacion.OCUPADA);
        habitacionRepository.save(habitacion);

        // 9. Cambiar estado reserva
        reserva.setEstado(EstadoReserva.EN_CASA);

        return reservaRepository.save(reserva);
    }

    @Override
    public Reserva realizarCheckout(Long id) {

        // 1. Validar que exista la reserva
        Reserva reserva = buscarEntidad(id);

        // 2. Validar estado correcto
        if (reserva.getEstado() != EstadoReserva.EN_CASA) {
            throw new EstadoReservaInvalidoException(
                    "Solo se puede hacer checkout a una reserva EN_CASA. Estado actual: "
                            + reserva.getEstado()
            );
        }

        // 3 Validación financiera
        FolioResumenResponse resumen = transaccionFolioService.obtenerFolioResumen(id);

        if (resumen.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                    "No se puede hacer checkout con balance o saldo pendiente: "
            );
        }


        // 4 Obtener habitación asignada
        Habitacion habitacion = reserva.getHabitacion();

        if (habitacion == null) {
            throw new HabitacionNoDisponibleException(
                    "La reserva no tiene habitación asignada"
            );
        }

        // 5 Cambiar estado de habitación (flujo real de hotel)
        // Opción bastate cvr ya que pasa a LIMPIEZA cuando el cliente sale
        habitacion.setEstado(EstadoHabitacion.SUCIA);

        // Si aún no manejas limpieza:
        // habitacion.setEstado(EstadoHabitacion.DISPONIBLE);

        habitacionRepository.save(habitacion);

        // 6 Cambiar estado de la reserva
        reserva.setEstado(EstadoReserva.SALIDA_CHECKOUT);

        // 7 Desvincular habitación (MUY IMPORTANTE)
        reserva.setHabitacion(null);

        // 8 Registrar fecha real de salida (opcional pero PRO)
        reserva.setFechaCheckout(LocalDateTime.now(ZoneId.systemDefault()));

        return reservaRepository.save(reserva);
    }


    @Override
    public Reserva confirmar(Long id) {
        log.info("[RESERVA_CONFIRMAR] iniciar confirmar(id={})", id);

        // Búsqueda PURA por ID: 404 solo si realmente no existe
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNoEncontradaException(id));

        System.out.println("DEBUG: Reserva encontrada: " + reserva.getId());

        log.info("[RESERVA_CONFIRMAR] reserva encontrada id={} estadoActual={}",
                reserva.getId(), reserva.getEstado());

        // Validación de negocio (estado) DESPUÉS de encontrar la reserva
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            log.warn("[RESERVA_CONFIRMAR] transición inválida id={} estadoActual={}",
                    reserva.getId(), reserva.getEstado());

            throw new EstadoReservaInvalidoException(
                    "Solo se puede confirmar una reserva PENDIENTE. Estado actual: " + reserva.getEstado()
            );
        }

        reserva.setEstado(EstadoReserva.CONFIRMADA);
        Reserva saved = reservaRepository.save(reserva);

        log.info("[RESERVA_CONFIRMAR] confirmada OK id={} nuevoEstado={}",
                saved.getId(), saved.getEstado());

        return saved;
    }

    @Override
    public void procesarNoPresentadas(){
        // lOGICA FUTURA
    }

    private long calcularNoches(Reserva reserva){
        return ChronoUnit.DAYS.between(
                reserva.getFechaEntrada(),
                reserva.getFechaSalida()
        );
    }

    private ReservaResponse mapToResponse(Reserva entity) {

        ReservaResponse res = new ReservaResponse();

        res.setId(entity.getId());
        res.setFechaEntrada(entity.getFechaEntrada());
        res.setFechaSalida(entity.getFechaSalida());
        res.setEstado(entity.getEstado().name());

        // Acá va lo importante para no romper el backend depués de agregar PlanTarifario a Reserva, se mapea la info del plan tarifario a la respuesta de reserva
        if (entity.getPlanTarifario() != null
                && entity.getPlanTarifario().getTipoHabitacion() != null) {

            res.setTipoHabitacionId(
                    entity.getPlanTarifario().getTipoHabitacion().getId()
            );

            res.setTipoHabitacionNombre(
                    entity.getPlanTarifario().getTipoHabitacion().getNombre()
            );

            res.setPrecioPorNoche(
                    entity.getPlanTarifario().getPrecioPorNoche()
            );

        } else {

            // Valores por defecto para evitar romper
            res.setTipoHabitacionId(null);
            res.setTipoHabitacionNombre("Sin plan");
            res.setPrecioPorNoche(null);

        }

        res.setNombreHuesped(entity.getNombreHuesped());
        res.setDocumentoHuesped(entity.getDocumentoHuesped());

        return res;
    }

    @Override
    public List<Reserva> obtenerReservasParaCheckout() {
        return reservaRepository.findByEstadoAndHabitacionIsNotNull(EstadoReserva.EN_CASA);
    }

    @Override
    public double calcularOcupacion(LocalDate inicio, LocalDate fin) {

        // 1. Obtener solo reservas relevantes
        List<Reserva> reservas = reservaRepository.findByEstadoIn(
                List.of(EstadoReserva.CONFIRMADA, EstadoReserva.EN_CASA)
        );

        long nochesOcupadas = 0;

        // 2. Calcular noches ocupadas dentro del rango
        for (Reserva r : reservas) {

            // Recortar rango de la reserva al rango solicitado
            LocalDate inicioReal = r.getFechaEntrada().isBefore(inicio)
                    ? inicio
                    : r.getFechaEntrada();

            LocalDate finReal = r.getFechaSalida().isAfter(fin)
                    ? fin
                    : r.getFechaSalida();

            // Verificar solapamiento
            if (inicioReal.isBefore(finReal)) {
                nochesOcupadas += ChronoUnit.DAYS.between(inicioReal, finReal);
            }
        }

        // 3. Calcular capacidad total
        long totalHabitaciones = habitacionRepository.count();
        long dias = ChronoUnit.DAYS.between(inicio, fin);

        if (dias == 0 || totalHabitaciones == 0) {
            return 0;
        }

        long capacidadTotal = totalHabitaciones * dias;

        // 4. % ocupación para mostrar en el dashboard XD
        return (nochesOcupadas * 100.0) / capacidadTotal;
    }

    @Override
    public DashboardResponse obtenerDashboard(LocalDate inicio, LocalDate fin) {

        BigDecimal ingresos = transaccionFolioService.obtenerIngresos(inicio, fin);
        double ocupacion = calcularOcupacion(inicio, fin);

        DashboardResponse response = new DashboardResponse();
        response.setIngresos(ingresos);
        response.setOcupacion(ocupacion);

        return response;
    }
}
