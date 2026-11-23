package com.parkflow360.modulos.vehiculos.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "La placa solo puede contener letras, números y guiones")
    @Size(max = 20, message = "La placa no puede tener más de 20 caracteres")
    private String placa;

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(regexp = "^(VEHICULO|MOTO|VEHICULO_ELECTRICO)$", message = "El tipo debe ser VEHICULO, MOTO o VEHICULO_ELECTRICO")
    private String tipo;

    @Size(max = 50, message = "El color no puede tener más de 50 caracteres")
    private String color;

    @Size(max = 100, message = "El modelo no puede tener más de 100 caracteres")
    private String modelo;

    @JsonSetter("placa")
    public void setPlaca(String placa) {
        this.placa = placa == null ? null : placa.trim().toUpperCase(Locale.ROOT);
    }
}
