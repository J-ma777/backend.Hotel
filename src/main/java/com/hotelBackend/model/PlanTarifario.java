package com.hotelBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "planes_tarifarios")
public class PlanTarifario {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;              // Tarifa Estándar, Tarifa Feriado

    @Column(name = "precio_por_noche", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioPorNoche;  // BigDecimal para evitar errores de redondeo en dinero

    @Column(name = "es_fin_de_semana", nullable = false)
    private Boolean esFinDeSemana;      // aplica sábado y domingo

    @Column(name = "es_feriado", nullable = false)
    private Boolean esFeriado;

    @Column(name = "valido_desde", nullable = false)
    private LocalDate validoDesde;

    @Column(name = "valido_hasta", nullable = false)
    private LocalDate validoHasta;

    @ManyToOne
    @JoinColumn(name = "tipo_habitacion_id", nullable = false)
    private TipoHabitacion tipoHabitacion;

}
