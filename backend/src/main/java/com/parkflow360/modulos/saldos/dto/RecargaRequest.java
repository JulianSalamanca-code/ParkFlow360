package com.parkflow360.modulos.saldos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class RecargaRequest {

    @NotNull(message = "El valor de la recarga es obligatorio.")
    @DecimalMin(value = "0.01", message = "El valor de la recarga debe ser mayor a cero.")
    private BigDecimal valor;

    @Size(max = 100, message = "La referencia no puede superar los 100 caracteres.")
    private String referencia;
}
