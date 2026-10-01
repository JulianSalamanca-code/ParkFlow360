package com.parkflow360.modulos.reportes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteResponse {
    private String tipo;
    private Long cantidad;
    private BigDecimal total;
    private String descripcion;
}
