package com.parkflow360.modulos.parqueaderos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Puestos que se crean junto con el parqueadero. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioCrearRequest {

    @NotBlank(message = "El número del puesto es obligatorio")
    @Size(max = 20, message = "El número del puesto no puede tener más de 20 caracteres")
    private String numero;

    @NotBlank(message = "El tipo del puesto es obligatorio")
    @Size(max = 50, message = "El tipo del puesto no puede tener más de 50 caracteres")
    private String tipo;

    @NotNull(message = "El piso del puesto es obligatorio")
    @Min(value = 0, message = "El piso no puede ser negativo")
    private Integer piso;

    private Integer fila;

    private Integer columna;
}
