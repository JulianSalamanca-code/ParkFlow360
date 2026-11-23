package com.parkflow360.modulos.ingresos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Consulta de solo lectura sobre la situación de pago de un ingreso. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoPagoResponse {

    private Long ingresoId;
    private String folio;
    private String placa;
    private String estadoIngreso;
    private boolean alDia;
    private String estado;
    private Long facturaId;
    private String estadoFactura;
    private BigDecimal valorFactura;
    private Long pagoId;
    private String estadoPago;
    private Long creditoId;
    private BigDecimal creditoPendiente;
    private boolean creditoVencido;
}
