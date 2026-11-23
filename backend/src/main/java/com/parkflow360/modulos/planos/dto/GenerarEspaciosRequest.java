package com.parkflow360.modulos.planos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Petición para generar en bloque los espacios de un plano (cuadrícula)
 * sin tener que crearlos uno por uno.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarEspaciosRequest {

    @Size(max = 10, message = "El prefijo no puede tener más de 10 caracteres")
    private String prefijo;

    @NotBlank(message = "El tipo de espacio es obligatorio")
    @Size(max = 50, message = "El tipo no puede tener más de 50 caracteres")
    private String tipo;

    private Integer piso;

    @Min(value = 1, message = "Debe haber al menos una fila")
    private Integer filas;

    @Min(value = 1, message = "Debe haber al menos una columna")
    private Integer columnas;

    @Size(max = 20, message = "El estado no puede tener más de 20 caracteres")
    private String estadoInicial;
}
