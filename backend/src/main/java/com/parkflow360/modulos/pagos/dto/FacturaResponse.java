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
public class FacturaResponse {
    private Long id;
    private Long ingresoId;
    private Long usuarioId;
    private Long parqueaderoId;
    private BigDecimal valor;
    private String estado;
    private LocalDateTime fechaEmision;
    private LocalDateTime fechaPago;
    private Long creditoId;
    private BigDecimal creditoPendiente;
    private boolean creditoVencido;
}
