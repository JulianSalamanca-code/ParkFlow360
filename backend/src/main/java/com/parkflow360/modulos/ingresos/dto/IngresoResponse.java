package com.parkflow360.modulos.ingresos.dto;

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
public class IngresoResponse {
    private Long id;
    private String folio;
    private String placa;
    private String categoria;
    private Long vehiculoId;
    private Long espacioId;
    private String espacioNumero;
    private Long tarifaId;
    private BigDecimal tarifaValor;
    private String modalidad;
    private String operador;
    private String novedad;
    private String estado;
    private Long usuarioId;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
}
