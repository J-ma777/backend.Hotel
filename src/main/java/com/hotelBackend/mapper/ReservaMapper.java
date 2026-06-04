package com.hotelBackend.mapper;

import com.hotelBackend.dto.response.ReservaCheckoutResponse;
import com.hotelBackend.dto.response.ReservaResponse;
import com.hotelBackend.model.Reserva;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public ReservaResponse toResponse(Reserva reserva) {

        ReservaResponse dto = new ReservaResponse();

        dto.setId(reserva.getId());
        dto.setEstado(reserva.getEstado().name());
        dto.setNombreHuesped(reserva.getNombreHuesped());
        dto.setDocumentoHuesped(reserva.getDocumentoHuesped());
        dto.setFechaEntrada(reserva.getFechaEntrada());
        dto.setFechaSalida(reserva.getFechaSalida());

        return dto;
    }

    public ReservaCheckoutResponse toCheckoutResponse(Reserva reserva) {

        ReservaCheckoutResponse dto = new ReservaCheckoutResponse();

        dto.setId(reserva.getId());
        dto.setNombreHuesped(reserva.getNombreHuesped());
        dto.setEstado(reserva.getEstado().name());
        dto.setFechaEntrada(reserva.getFechaEntrada());
        dto.setFechaSalida(reserva.getFechaSalida());

        if (reserva.getHabitacion() != null) {
            dto.setHabitacionId(reserva.getHabitacion().getId());
            dto.setHabitacionNumero(reserva.getHabitacion().getNumero());
        }

        return dto;
    }
}
