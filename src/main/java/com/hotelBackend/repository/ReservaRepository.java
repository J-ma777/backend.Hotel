package com.hotelBackend.repository;

import com.hotelBackend.model.Reserva;
import com.hotelBackend.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.time.LocalDate;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEstadoAndFechaEntradaBefore(
            EstadoReserva estado,
            LocalDate fecha
    );
}
