package com.parkflow360.modulos.saldos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoSaldoResponse {
    private Long id;
    private String tipo;
    private BigDecimal valor;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoPosterior;
    private String referencia;
    private LocalDateTime fecha;
}
