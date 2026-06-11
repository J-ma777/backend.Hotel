package com.hotelbackend.service.Implementaciones;

import com.hotelbackend.dto.request.PlanTarifarioRequest;
import com.hotelbackend.dto.response.PlanTarifarioResponse;
import com.hotelbackend.model.PlanTarifario;
import com.hotelbackend.model.TipoHabitacion;
import com.hotelbackend.model.enums.TipoTarifa;
import com.hotelbackend.repository.PlanTarifarioRepository;
import com.hotelbackend.repository.TipoHabitacionRepository;
import com.hotelbackend.service.PlanTarifarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PlanTarifarioServiceImpl implements PlanTarifarioService {

    private final PlanTarifarioRepository planTarifarioRepository;
    private final TipoHabitacionRepository tipoHabitacionRepository;

    // HU-20: Obtener tarifa por noche según tipo de día
    @Override
    public PlanTarifario obtenerTarifaParaNoche(Long tipoHabitacionId, LocalDate fechaNoche) {

        // 1. Determinar tipo de día
        TipoTarifa tipoTarifa = obtenerTipoDia(fechaNoche);

        // 2. Buscar tarifa en BD
        return planTarifarioRepository
                .findTarifaPorFecha(tipoHabitacionId, tipoTarifa, fechaNoche)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe tarifa para tipoHabitacionId=" + tipoHabitacionId +
                                ", fecha=" + fechaNoche +
                                ", tipoTarifa=" + tipoTarifa
                ));
    }

    private PlanTarifarioResponse mapToResponse(PlanTarifario entity) {

        PlanTarifarioResponse res = new PlanTarifarioResponse();

        res.setId(entity.getId());
        res.setNombre(entity.getNombre());
        res.setPrecioPorNoche(entity.getPrecioPorNoche());
        res.setValidoDesde(entity.getValidoDesde());
        res.setValidoHasta(entity.getValidoHasta());
        // Para filtrar tipos de habitaciones en reservas de acuerdo a la categoria de la habitacion
        res.setTipoHabitacionId(entity.getTipoHabitacion().getId());
        res.setTipoHabitacionNombre(entity.getTipoHabitacion().getNombre());

        boolean activo = !LocalDate.now(ZoneId.systemDefault()).isBefore(entity.getValidoDesde()) &&
                !LocalDate.now(ZoneId.systemDefault()).isAfter(entity.getValidoHasta());

        res.setActivo(activo);
        return res;
    }

    // HU-21: Administrar planes tarifarios (Admin)

    @Override
    public PlanTarifarioResponse crear(PlanTarifarioRequest request) {

        validarFechasRequest(request);
        PlanTarifario entity = new PlanTarifario();

        entity.setNombre(request.getNombre());
        entity.setPrecioPorNoche(request.getPrecioPorNoche());
        entity.setValidoDesde(request.getValidoDesde());
        entity.setValidoHasta(request.getValidoHasta());
        entity.setTipoTarifa(request.getTipoTarifa());

        TipoHabitacion tipo = tipoHabitacionRepository
                .findById(request.getTipoHabitacionId())
                .orElseThrow();

        entity.setTipoHabitacion(tipo);

        PlanTarifario saved = planTarifarioRepository.save(entity);

        return mapToResponse(saved);

    }

    private void validarFechasRequest(PlanTarifarioRequest request) {

        if (request.getValidoDesde() == null || request.getValidoHasta() == null) {
            throw new IllegalArgumentException("Las fechas son obligatorias");
        }

        if (request.getValidoHasta().isBefore(request.getValidoDesde())) {
            throw new IllegalArgumentException(
                    "La fecha validoHasta no puede ser anterior a validoDesde"
            );
        }
    }

    @Override
    public PlanTarifarioResponse actualizar(Long id, PlanTarifarioRequest request) {

        validarFechasRequest(request); // validar fechas con el metodo ya mencionado

        PlanTarifario existente = planTarifarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException("Plan tarifario no encontrado"));

        validarFechasRequest(request);

        existente.setNombre(request.getNombre());
        existente.setPrecioPorNoche(request.getPrecioPorNoche());
        existente.setTipoTarifa(request.getTipoTarifa());
        existente.setValidoDesde(request.getValidoDesde());
        existente.setValidoHasta(request.getValidoHasta());

        TipoHabitacion tipo = tipoHabitacionRepository
                .findById(request.getTipoHabitacionId())
                .orElseThrow();

        existente.setTipoHabitacion(tipo);

        PlanTarifario actualizado = planTarifarioRepository.save(existente);

        return mapToResponse(actualizado);
    }

    @Override
    public List<PlanTarifarioResponse> listarTodos() {
        return planTarifarioRepository
                .findAllByOrderByValidoDesdeDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PlanTarifarioResponse> listarPorTipo(Long tipoHabitacionId) {
        return planTarifarioRepository.findByTipoHabitacionId(tipoHabitacionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private TipoTarifa obtenerTipoDia(LocalDate fecha) {

        // Ejemplo simple de feriados (puedes ajustar luego)
        List<LocalDate> feriados = List.of(
                LocalDate.of(2026, 1, 1),   // Año nuevo
                LocalDate.of(2026, 7, 28),  // Fiestas patrias PE
                LocalDate.of(2026, 12, 25)  // Navidad
        );

        if (feriados.contains(fecha)) {
            return TipoTarifa.HOLIDAY;
        }

        DayOfWeek dia = fecha.getDayOfWeek();

        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return TipoTarifa.WEEKEND;
        }

        return TipoTarifa.WEEKDAY;
    }

    @Override
    public BigDecimal obtenerPrecioPorFecha(Long tipoHabitacionId, LocalDate fecha) {
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calcularTotalReserva(Long tipoHabitacionId, LocalDate entrada, LocalDate salida) {

        BigDecimal total = BigDecimal.ZERO;

        for (LocalDate fecha = entrada; fecha.isBefore(salida); fecha = fecha.plusDays(1)) {

            PlanTarifario tarifa = obtenerTarifaParaNoche(tipoHabitacionId, fecha);

            total = total.add(tarifa.getPrecioPorNoche());
        }

        return total;
    }
}
