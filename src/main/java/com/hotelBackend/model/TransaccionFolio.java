package com.hotelBackend.model;

import com.hotelBackend.model.enums.TipoTransaccion;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "transacciones_folio")
public class TransaccionFolio {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransaccion tipo;       // CARGO_NOCHE, CARGO_CONSUMO, PAGO, DESCUENTO

    @Column(nullable = false, length = 255)
    private String descripcion;         // "Noche del 15/07", "Desayuno buffet"

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;  // foto del precio en ese momento (inmutabilidad)

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;           // precioUnitario * cantidad (guardado calculado)

    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDateTime fechaTransaccion;

    @Column(name = "registrado_por", nullable = false)
    private Long registradoPor;         // ID del empleado (auditoría)

    @ManyToOne
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

}
