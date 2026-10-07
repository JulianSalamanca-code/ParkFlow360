package com.parkflow360.modulos.planos.dto;

import jakarta.validation.constraints.Min;
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
public class PlanoRequest {

    @NotBlank(message = "El nombre del plano es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede tener más de 255 caracteres")
    private String descripcion;

    @Size(max = 500, message = "La URL de la imagen no puede tener más de 500 caracteres")
    private String imagenUrl;

    private Integer piso;

    @Min(value = 0, message = "Las filas no pueden ser negativas")
    private Integer filas;

    @Min(value = 0, message = "Las columnas no pueden ser negativas")
    private Integer columnas;

    private Integer ancho;

    private Integer alto;
}
