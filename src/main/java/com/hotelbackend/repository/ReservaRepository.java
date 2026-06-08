package com.hotelbackend.repository;

import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.Reserva;
import com.hotelbackend.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
SELECT COUNT(r) > 0 FROM Reserva r
WHERE r.habitacion.id = :habitacionId
AND r.habitacion IS NOT NULL
AND r.estado IN ('CONFIRMADA', 'EN_CASA')
AND r.id <> :reservaId
AND (
    :inicio < r.fechaSalida
    AND :fin > r.fechaEntrada
)
""")
    boolean existsConflicto(
            Long habitacionId,
            LocalDate inicio,
            LocalDate fin,
            Long reservaId
    );

    // Metodo derivado (findBy...)
    List<Reserva> findByEstadoAndFechaEntradaBefore(
            EstadoReserva estado,
            LocalDate fecha
    );

    boolean existsByHabitacionIdAndEstadoIn(
            Long habitacionId,
            List<EstadoReserva> estados
    );

    List<Reserva> findByEstadoAndHabitacionIsNotNull(EstadoReserva estado);

    Optional<Reserva> findByHabitacionAndEstado(
            Habitacion habitacion,
            EstadoReserva estado
    );
}
