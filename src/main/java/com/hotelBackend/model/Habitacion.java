package com.hotelBackend.model;

import com.hotelBackend.model.enums.EstadoHabitacion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "habitaciones")
public class Habitacion {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;              // 101, 202, Suite-01

    @Column(nullable = false)
    private Integer piso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoHabitacion estado;    // DISPONIBLE, OCUPADA, SUCIA, LIMPIANDO, INSPECCIONADA, FUERA_DE_SERVICIO

    @ManyToOne
    @JoinColumn(name = "tipo_habitacion_id", nullable = false)
    private TipoHabitacion tipoHabitacion;
}
