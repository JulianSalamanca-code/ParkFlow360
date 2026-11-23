package com.parkflow360.modulos.ingresos.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos que el guardia puede completar al verificar un ingreso en campo. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificarIngresoRequest {

    @Size(max = 20, message = "La placa no puede superar los 20 caracteres")
    private String placa;

    @Size(max = 20, message = "La categoría no puede superar los 20 caracteres")
    private String categoria;

    private Long vehiculoId;

    @Size(max = 20, message = "El número de espacio no puede superar los 20 caracteres")
    private String espacioNumero;

    @Size(max = 100, message = "El operador no puede superar los 100 caracteres")
    private String operador;

    @Size(max = 200, message = "La novedad no puede superar los 200 caracteres")
    private String novedad;
}
