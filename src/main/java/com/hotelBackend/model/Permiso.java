package com.hotelBackend.model;

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
@Table(name = "permisos")
public class Permiso {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;      // Ej: CREAR_RESERVA

    @Column(nullable = false, length = 50)
    private String recurso;     // Ej: RESERVAS

    @Column(nullable = false, length = 20)
    private String accion;      // Ej: CREAR, LEER, EDITAR

}
