package com.parkflow360.modulos.vehiculos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^[A-Z]{3}-[A-Z0-9]{3}$", message = "La placa debe tener formato AAA-000 o AAA-00A (6 caracteres)")
    @Size(max = 7, message = "La placa no puede tener más de 7 caracteres")
    private String placa;

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "^(VEHICULO|MOTO|VEHICULO_ELECTRICO)$", message = "El tipo debe ser VEHICULO, MOTO o VEHICULO_ELECTRICO")
    private String tipo;

    @Size(max = 50, message = "El color no puede tener más de 50 caracteres")
    private String color;

    @Size(max = 100, message = "El modelo no puede tener más de 100 caracteres")
    private String modelo;
}
