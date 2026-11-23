package com.parkflow360.modulos.creditos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditoRequest {

    @NotNull(message = "La factura es obligatoria.")
    private Long facturaId;
}
