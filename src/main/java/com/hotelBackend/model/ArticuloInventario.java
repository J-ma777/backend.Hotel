package com.hotelBackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "articulo_inventario")
public class ArticuloInventario {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;              // Papel higiénico, Jabón, Toalla

    @Column(nullable = false, length = 30)
    private String unidad;              // kg, litros, unidades, rollos

    @Column(name = "stock_actual", nullable = false)
    private Double stockActual;         // cantidad disponible actualmente

    @Column(name = "stock_minimo", nullable = false)
    private Double stockMinimo;         // punto de alerta para reposición

    @Column(name = "costo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoUnitario;
}
