package com.hotelbackend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardResponse {
    private BigDecimal ingresos;
    private double ocupacion;
}
