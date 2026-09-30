package com.parkflow360.modulos.vehiculos.dto;

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
public class VehiculoRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 20, message = "La placa no puede tener más de 20 caracteres")
    private String placa;

    @NotBlank(message = "El tipo es obligatorio")
    @Size(max = 50, message = "El tipo no puede tener más de 50 caracteres")
    private String tipo;

    @Size(max = 50, message = "El color no puede tener más de 50 caracteres")
    private String color;

    @Size(max = 100, message = "El modelo no puede tener más de 100 caracteres")
    private String modelo;
}
