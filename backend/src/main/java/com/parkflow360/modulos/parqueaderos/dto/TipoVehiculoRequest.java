package com.parkflow360.modulos.parqueaderos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoVehiculoRequest {

    @NotBlank(message = "El tipo de vehículo es obligatorio")
    @Size(max = 50, message = "El tipo de vehículo no puede tener más de 50 caracteres")
    private String tipo;

    private boolean cantidadFija;

    private Integer cantidad;
}
