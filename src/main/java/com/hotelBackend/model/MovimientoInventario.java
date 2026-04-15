package com.hotelBackend.model;

import com.hotelBackend.model.enums.TipoMovimiento;
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
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;        // ENTRADA sube stock, SALIDA lo baja

    @Column(nullable = false)
    private Double cantidad;

    @Column(length = 255)
    private String motivo;              // "Compra a proveedor", "Consumo habitación 201"

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;

    @Column(name = "registrado_por", nullable = false)
    private Long registradoPor;         // ID del empleado (auditoría)

    @ManyToOne
    @JoinColumn(name = "articulo_id", nullable = false)
    private ArticuloInventario articulo;
}
