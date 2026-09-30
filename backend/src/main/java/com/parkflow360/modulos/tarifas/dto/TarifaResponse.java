package com.parkflow360.modulos.tarifas.dto;

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
public class TarifaResponse {
    private Long id;
    private String nombre;
    private String tipo;
    private BigDecimal valor;
    private String duracion;
    private LocalDateTime fechaCreacion;
}
