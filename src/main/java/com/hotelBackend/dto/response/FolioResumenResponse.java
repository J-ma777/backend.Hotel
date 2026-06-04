package com.hotelBackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class FolioResumenResponse {
    private BigDecimal totalCargos;
    private BigDecimal totalPagos;
    private BigDecimal totalDescuentos;
    private BigDecimal balance;

}
