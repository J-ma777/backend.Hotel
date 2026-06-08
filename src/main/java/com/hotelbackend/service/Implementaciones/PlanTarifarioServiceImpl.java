package com.hotelbackend.service.Implementaciones;

import com.hotelbackend.dto.request.PlanTarifarioRequest;
import com.hotelbackend.dto.response.PlanTarifarioResponse;
import com.hotelbackend.model.PlanTarifario;
import com.hotelbackend.model.TipoHabitacion;
import com.hotelbackend.repository.PlanTarifarioRepository;
import com.hotelbackend.repository.TipoHabitacionRepository;
import com.hotelbackend.service.PlanTarifarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public PlanTarifario obtenerTarifaParaNoche(
            Long tipoHabitacionId,
            LocalDate fechaNoche
    ) {

        DayOfWeek dayOfWeek = fechaNoche.getDayOfWeek();

        boolean esFinSemana =
                dayOfWeek == DayOfWeek.FRIDAY ||
                        dayOfWeek == DayOfWeek.SATURDAY;

        // 1️ PRIORIDAD: FERIADO
        var feriado = planTarifarioRepository.buscarPlanes(
                tipoHabitacionId,
                fechaNoche,
                true,
                false
        );

        if (!feriado.isEmpty()) {
            return feriado.get(0);
        }

        // 2️ PRIORIDAD: FIN DE SEMANA
        if (esFinSemana) {
            var finSemana = planTarifarioRepository.buscarPlanes(
                    tipoHabitacionId,
                    fechaNoche,
                    false,
                    true
            );

            if (!finSemana.isEmpty()) {
                return finSemana.get(0);
            }
        }

        // 3️ PRIORIDAD: ENTRE SEMANA
        var entreSemana = planTarifarioRepository.buscarPlanes(
                tipoHabitacionId,
                fechaNoche,
                false,
                false
        );

        if (!entreSemana.isEmpty()) {
            return entreSemana.get(0);
        }

        // CA-06: bloquear Check-in si no existe tarifa
        throw new IllegalStateException(
                "No existe un plan tarifario vigente para la fecha " + fechaNoche
        );
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
        entity.setEsFeriado(request.getEsFeriado());
        entity.setEsFinDeSemana(request.getEsFinDeSemana());

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
        existente.setEsFeriado(request.getEsFeriado());
        existente.setEsFinDeSemana(request.getEsFinDeSemana());
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

}
