package com.hotelbackend.repository;

import com.hotelbackend.model.Habitacion;
import com.hotelbackend.model.enums.EstadoHabitacion;
import com.hotelbackend.model.enums.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    // MÉTODO PARA RESERVAS
    @Query("""
        SELECT h FROM Habitacion h
        WHERE h.id NOT IN (
            SELECT r.habitacion.id FROM Reserva r
            WHERE r.habitacion IS NOT NULL
            AND r.estado IN :estadosReserva
            AND (
                :inicio < r.fechaSalida
                AND :fin > r.fechaEntrada
            )
        )
    """)
    List<Habitacion> findDisponibles(
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("estadosReserva") List<EstadoReserva> estadosReserva
    ); // Método clave para la búsqueda de habitaciones disponibles en un rango de fechas
                                                                        // que será llamado en el servicio HabitacionServiceImpl.buscarDisponibles

    // MÉTODO PARA MANTENIMIENTO

    @Query("""
        SELECT h FROM Habitacion h
        WHERE h.estado <> :estadoExcluido
        AND h.id NOT IN (
            SELECT r.habitacion.id FROM Reserva r
            WHERE r.estado IN :estadosReserva
        )
    """)
        //  Método para encontrar habitaciones que no están en ciertos estados, se llama en el servicio HabitacionServiceImpl.obtenerParaMantenimiento
    List<Habitacion> findByParaMantenimiento(
            @Param("estadoExcluido") EstadoHabitacion estadoExcluido,
            @Param("estadosReserva") List<EstadoReserva> estadosReserva
    );

    /* La siguiente metdo (Consulta) filtra por tipo de habitación, por fechas
    * evita ocupadas y evita fuera de servicios */
    @Query("""
        SELECT h FROM Habitacion h
        WHERE h.tipoHabitacion.id = :tipoHabitacionId
        AND h.estado = :estadoHabitacion
        AND NOT EXISTS (
            SELECT 1 FROM Reserva r
            WHERE r.habitacion IS NOT NULL
            AND r.habitacion = h
            AND r.estado IN :estadosReserva
            AND (
                :inicio < r.fechaSalida
                AND :fin > r.fechaEntrada
            )
        )
    """)
    List<Habitacion> findDisponiblesPorTipo(
            @Param("tipoHabitacionId") Long tipoHabitacionId,
            @Param("inicio") LocalDate inicio,
            @Param("fin") LocalDate fin,
            @Param("estadoHabitacion") EstadoHabitacion estadoHabitacion,
            @Param("estadosReserva") List<EstadoReserva> estadosReserva
    );


    boolean existsByNumeroAndIdNot(String numero, Long id);
}
