package com.parkflow360.modulos.saldos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoResponse {
    private BigDecimal saldo;
    private List<MovimientoSaldoResponse> movimientos;
}
