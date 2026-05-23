package com.hotelBackend.repository;

import com.hotelBackend.model.Habitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    @Query("""
SELECT h FROM Habitacion h
WHERE h.id NOT IN (
    SELECT r.habitacion.id FROM Reserva r
    WHERE r.habitacion IS NOT NULL
    AND r.estado IN ('CONFIRMADA', 'EN_CASA')
    AND (
        :inicio < r.fechaSalida
        AND :fin > r.fechaEntrada
    )
)
""")

    List<Habitacion> findDisponibles(LocalDate inicio, LocalDate fin); // Método clave para la búsqueda de habitaciones disponibles en un rango de fechas
                                                                        // que será llamado en el servicio HabitacionServiceImpl.buscarDisponibles
}
