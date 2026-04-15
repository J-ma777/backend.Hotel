package com.hotelBackend.model;

import com.hotelBackend.model.enums.EstadoTicket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "ticket_mantenimiento")
public class TicketMantenimiento {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;             // "Grifo roto", "A/C no enfría"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EstadoTicket estado;            // ABIERTO, EN_PROCESO, RESUELTO

    @Column(name = "reportado_en", nullable = false)
    private LocalDateTime reportadoEn;

    @Column(name = "resuelta_en")
    private LocalDateTime resueltoEn;       // NULL hasta que se resuelva la avería

    @Column(name = "reportado_por", nullable = false)
    private Long reportadoPor;              // ID del empleado (auditoría)

    @ManyToOne
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

}
