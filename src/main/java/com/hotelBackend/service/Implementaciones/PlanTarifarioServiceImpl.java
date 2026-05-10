package com.hotelBackend.service.Implementaciones;

import com.hotelBackend.model.PlanTarifario;
import com.hotelBackend.repository.PlanTarifarioRepository;
import com.hotelBackend.service.PlanTarifarioService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class PlanTarifarioServiceImpl implements PlanTarifarioService {

    private final PlanTarifarioRepository planTarifarioRepository;

    public PlanTarifarioServiceImpl(PlanTarifarioRepository planTarifarioRepository) {
        this.planTarifarioRepository = planTarifarioRepository;
    }

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

    // HU-21: Administrar planes tarifarios (Admin)

    @Override
    public PlanTarifario crear(PlanTarifario plan) {
        validarFechas(plan);
        return planTarifarioRepository.save(plan);
    }

    @Override
    public PlanTarifario actualizar(Long id, PlanTarifario plan) {

        PlanTarifario existente = planTarifarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalStateException("Plan tarifario no encontrado"));

        validarFechas(plan);

        existente.setNombre(plan.getNombre());
        existente.setPrecioPorNoche(plan.getPrecioPorNoche());
        existente.setEsFeriado(plan.getEsFeriado());
        existente.setEsFinDeSemana(plan.getEsFinDeSemana());
        existente.setValidoDesde(plan.getValidoDesde());
        existente.setValidoHasta(plan.getValidoHasta());
        existente.setTipoHabitacion(plan.getTipoHabitacion());

        return planTarifarioRepository.save(existente);
    }

    @Override
    public List<PlanTarifario> listarTodos() {
        return planTarifarioRepository.findAllByOrderByValidoDesdeDesc();
    }

    // Validaciones comunes
    private void validarFechas(PlanTarifario plan) {
        if (plan.getValidoHasta().isBefore(plan.getValidoDesde())) {
            throw new IllegalArgumentException(
                    "La fecha validoHasta no puede ser anterior a validoDesde"
            );
        }
    }
}
