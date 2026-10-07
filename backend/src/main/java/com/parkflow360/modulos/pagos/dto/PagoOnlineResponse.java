package com.parkflow360.modulos.pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Respuesta al iniciar un pago en línea: incluye la URL de checkout de Wompi. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoOnlineResponse {
    private Long pagoId;
    private String referencia;
    private String checkoutUrl;
    private BigDecimal valor;
    private Long valorEnCentavos;
    private String moneda;
    private String estado;
}
