package com.parkflow360.modulos.creditos.dto;

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
public class CreditoResponse {
    private Long id;
    private Long usuarioId;
    private Long facturaId;
    private BigDecimal monto;
    private BigDecimal montoPendiente;
    private LocalDateTime fechaOtorgamiento;
    private LocalDateTime fechaLimite;
    private String estado;
    private boolean vencido;
}
