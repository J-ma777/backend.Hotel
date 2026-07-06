package com.hotelbackend.dto.request;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class CrearReservaRequest {

    @NotNull
    @FutureOrPresent
    private LocalDate fechaEntrada;

    @NotNull
    @Future
    private LocalDate fechaSalida;

    @NotNull
    @Min(1)
    private Integer cantidadHuespedes;

    @NotBlank
    private String nombreHuesped;

    @NotBlank
    private String documentoHuesped;

    @NotNull
    private Long tipoHabitacionId;

    @NotNull
    private Long planTarifarioId;

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public Integer getCantidadHuespedes() {
        return cantidadHuespedes;
    }

    public void setCantidadHuespedes(Integer cantidadHuespedes) {
        this.cantidadHuespedes = cantidadHuespedes;
    }

    public String getNombreHuesped() {
        return nombreHuesped;
    }

    public void setNombreHuesped(String nombreHuesped) {
        this.nombreHuesped = nombreHuesped;
    }

    public String getDocumentoHuesped() {
        return documentoHuesped;
    }

    public void setDocumentoHuesped(String documentoHuesped) {
        this.documentoHuesped = documentoHuesped;
    }

    public Long getTipoHabitacionId() {
        return tipoHabitacionId;
    }

    public void setTipoHabitacionId(Long tipoHabitacionId) {
        this.tipoHabitacionId = tipoHabitacionId;
    }

    public Long getPlanTarifarioId() {
        return planTarifarioId;
    }

    public void setPlanTarifarioId(Long planTarifarioId) {
        this.planTarifarioId = planTarifarioId;
    }

}
