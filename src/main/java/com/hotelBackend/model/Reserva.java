package com.hotelBackend.model;

import com.hotelBackend.model.enums.EstadoReserva;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "reservas")
public class Reserva {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada;

    @Column(name = "fecha_salida", nullable = false)
    private LocalDate fechaSalida;

    @Column(name = "cantidad_huespedes", nullable = false)
    private Integer cantidadHuespedes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;       // CONFIRMADA, IN_HOUSE, CHECK_OUT, CANCELADA, NO_SHOW

    @Column(name = "nombre_huesped", nullable = false, length = 150)
    private String nombreHuesped;

    @Column(name = "documento_huesped", nullable = false, length = 20)
    private String documentoHuesped;   // DNI o pasaporte (req. legal Perú)

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "creado_por", nullable = false)
    private Long creadoPor;            // ID del recepcionista (auditoría)

    @ManyToOne
    @JoinColumn(name = "habitacion_id", nullable = true) // Acá tube problemas, ya que cuando estaba en folse,
    // no cambiaba de pendiente a confirmada, y no se asignaba la habitacion, por lo que al ser null, no se asignaba la habitacion, y no se cambiaba el estado a confirmada.
    private Habitacion habitacion;

}
