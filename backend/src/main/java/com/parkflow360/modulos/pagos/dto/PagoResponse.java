package com.parkflow360.modulos.pagos.dto;

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
public class PagoResponse {
    private Long id;
    private Long parqueaderoId;
    private Long vehiculoId;
    private Long espacioId;
    private Long tarifaId;
    private Long ingresoId;
    private BigDecimal valor;
    private LocalDateTime fecha;
    private String metodoPago;
    private String estadoPago;
}
