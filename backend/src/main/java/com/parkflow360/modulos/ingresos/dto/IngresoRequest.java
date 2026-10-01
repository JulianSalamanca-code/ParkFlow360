package com.parkflow360.modulos.ingresos.dto;

import jakarta.validation.constraints.NotBlank;
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
public class IngresoRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 20, message = "La placa no puede tener más de 20 caracteres")
    private String placa;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 20, message = "La categoría no puede tener más de 20 caracteres")
    private String categoria;

    private Long vehiculoId;

    @NotNull(message = "El espacio es obligatorio")
    private Long espacioId;

    @Size(max = 20, message = "El número de espacio no puede tener más de 20 caracteres")
    private String espacioNumero;

    private Long tarifaId;

    private BigDecimal tarifaValor;

    @Size(max = 30, message = "La modalidad no puede tener más de 30 caracteres")
    private String modalidad;

    @Size(max = 100, message = "El operador no puede tener más de 100 caracteres")
    private String operador;

    @Size(max = 200, message = "La novedad no puede tener más de 200 caracteres")
    private String novedad;
}
