package com.hotelBackend.model;

import com.hotelBackend.model.enums.EstadoHabitacion;
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
@Table(name = "registro_limpieza")
public class RegistroLimpieza {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_anterior", nullable = false, length = 20)
    private EstadoHabitacion estadoAnterior;    // estado antes del cambio

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nuevo", nullable = false, length = 20)
    private EstadoHabitacion estadoNuevo;       // estado después del cambio

    @Column(columnDefinition = "TEXT")
    private String notas;                       // "manchas en alfombra", "falta jabón"

    @Column(name = "cambiado_en", nullable = false)
    private LocalDateTime cambiadoEn;

    @Column(name = "cambiado_por", nullable = false)
    private Long cambiadoPor;                   // ID del empleado (auditoría)

    @ManyToOne
    @JoinColumn(name = "habitacion_id", nullable = false)
    private Habitacion habitacion;

}
