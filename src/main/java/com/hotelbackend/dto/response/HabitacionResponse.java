package com.hotelbackend.dto.response;

import com.hotelbackend.model.Habitacion;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HabitacionResponse {

    private Long id;
    private String numero;
    private String estado;
    private Integer piso;
    private String tipoNombre;

    // Constructor ORIGINAL
    // Este se usa en otras partes del sistema
    public HabitacionResponse(Habitacion h) {
        this.id = h.getId();
        this.numero = h.getNumero();
        this.estado = h.getEstado().name(); // estado físico
        this.piso = h.getPiso();

        if (h.getTipoHabitacion() != null) {
            this.tipoNombre = h.getTipoHabitacion().getNombre();
        }
    }

    // NUEVO constructor PROFESIONAL
    // Este permite calcular estado dinámico (OCUPADA)
    public HabitacionResponse(Habitacion h, boolean ocupada) {
        this.id = h.getId();
        this.numero = h.getNumero();
        this.piso = h.getPiso();

        if (ocupada) {
            this.estado = "OCUPADA"; //  override visual correcto
        } else {
            this.estado = h.getEstado().name();
        }

        if (h.getTipoHabitacion() != null) {
            this.tipoNombre = h.getTipoHabitacion().getNombre();
        }
    }
}

