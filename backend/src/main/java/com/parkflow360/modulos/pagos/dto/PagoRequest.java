package com.parkflow360.modulos.pagos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoRequest {

    private Long vehiculoId;

    private Long espacioId;

    private Long tarifaId;

    private Long ingresoId;

    @NotNull(message = "El valor es obligatorio")
    @Positive(message = "El valor debe ser positivo")
    private BigDecimal valor;

    @Size(max = 50, message = "El método de pago no puede tener más de 50 caracteres")
    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    @Pattern(regexp = "CONFIRMADO|FALLIDO", message = "El estado del pago debe ser CONFIRMADO o FALLIDO")
    private String estadoPago;
}
