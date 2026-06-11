package com.hotelbackend.repository;

import com.hotelbackend.model.PlanTarifario;
import com.hotelbackend.model.enums.TipoTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanTarifarioRepository extends JpaRepository<PlanTarifario, Long> {

    @Query("""
    SELECT p FROM PlanTarifario p
    WHERE p.tipoHabitacion.id = :tipoHabitacionId
    AND p.tipoTarifa = :tipoTarifa
    AND :fecha BETWEEN p.validoDesde AND p.validoHasta
""")
    Optional<PlanTarifario> findTarifaPorFecha(
            @Param("tipoHabitacionId") Long tipoHabitacionId,
            @Param("tipoTarifa") TipoTarifa tipoTarifa,
            @Param("fecha") LocalDate fecha
    );

    List<PlanTarifario> findAllByOrderByValidoDesdeDesc();

    List<PlanTarifario> findByTipoHabitacionId(Long tipoHabitacionId);
}
