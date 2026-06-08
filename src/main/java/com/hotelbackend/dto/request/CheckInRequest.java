package com.hotelbackend.dto.request;

import jakarta.validation.constraints.NotNull;

public class CheckInRequest {

    @NotNull(message = "habitacionId es obligatorio")
    private Long habitacionId;

    public Long getHabitacionId() {
        return habitacionId;
    }

    public void setHabitacionId(Long habitacionId) {
        this.habitacionId = habitacionId;
    }

}
