package com.parkflow360.modulos.pagos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Solicitud del cliente para iniciar un pago en línea con Wompi. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoOnlineRequest {

    @NotNull(message = "El valor es obligatorio")
    @Positive(message = "El valor debe ser positivo")
    private BigDecimal valor;

    /** Opcional: parqueo/reserva que se está pagando. */
    private Long ingresoId;

    private Long vehiculoId;

    private Long espacioId;

    private Long tarifaId;
}
