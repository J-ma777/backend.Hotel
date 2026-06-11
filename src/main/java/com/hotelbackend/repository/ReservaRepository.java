package com.hotelbackend.repository;

import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.Reserva;
import com.hotelbackend.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
SELECT COUNT(r) > 0 FROM Reserva r
WHERE r.habitacion.id = :habitacionId
AND r.habitacion IS NOT NULL
AND r.estado IN :estados
AND r.id <> :reservaId
AND (
    :inicio < r.fechaSalida
    AND :fin > r.fechaEntrada
)
""")
    boolean existsConflicto(
            @Param("habitacionId") Long habitacionId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("reservaId") Long reservaId,
            @Param("estados") List<EstadoReserva> estados

    );

    // Para ver la ocupación
    List<Reserva> findByEstadoIn(List<EstadoReserva> estados);

    // Este es para CHECHOUT
    List<Reserva> findByEstadoAndHabitacionIsNotNull(EstadoReserva estado);

    boolean existsByHabitacionIdAndEstadoIn(
            Long habitacionId,
            List<EstadoReserva> estados
    );

    //Util para validaciones puntuales
    Optional<Reserva> findByHabitacionAndEstado(
            Habitacion habitacion,
            EstadoReserva estado
    );
}
