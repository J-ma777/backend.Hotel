package com.hotelBackend.repository;

import com.hotelBackend.model.PlanTarifario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.time.LocalDate;

public interface PlanTarifarioRepository extends JpaRepository<PlanTarifario, Long> {

    @Query("""
        SELECT p
        FROM PlanTarifario p
        WHERE p.tipoHabitacion.id = :tipoHabitacionId
          AND :fecha BETWEEN p.validoDesde AND p.validoHasta
          AND p.esFeriado = :esFeriado
          AND p.esFinDeSemana = :esFinDeSemana
        """)
    List<PlanTarifario> buscarPlanes(
            @Param("tipoHabitacionId") Long tipoHabitacionId,
            @Param("fecha") LocalDate fecha,
            @Param("esFeriado") Boolean esFeriado,
            @Param("esFinDeSemana") Boolean esFinDeSemana
    );

    List<PlanTarifario> findAllByOrderByValidoDesdeDesc();
}
