package com.hotelBackend.repository;

import com.hotelBackend.model.TicketMantenimiento;
import com.hotelBackend.model.enums.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketMantenimientoRepository extends JpaRepository<TicketMantenimiento,Long> {

    // Realizar consulta para verificar si existe un ticket abierto para una habitación específica
    boolean existsByHabitacionIdAndEstado(Long habitacionId, EstadoTicket estado);

}
