package com.hotelBackend.repository;

import com.hotelBackend.model.TicketMantenimiento;
import com.hotelBackend.model.enums.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketMantenimientoRepository extends JpaRepository<TicketMantenimiento,Long> {

    // Realizar consulta para verificar si existe un ticket abierto para una habitación específica
    boolean existsByHabitacionIdAndEstadoIn(
            Long habitacionId,
            List<EstadoTicket> estados
    );


}
