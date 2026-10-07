package com.parkflow360.modulos.espacios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioRequest {

    @NotBlank(message = "El número es obligatorio")
    @Size(max = 20, message = "El número no puede tener más de 20 caracteres")
    private String numero;

    @NotBlank(message = "El tipo es obligatorio")
    @Size(max = 50, message = "El tipo no puede tener más de 50 caracteres")
    private String tipo;

    @NotBlank(message = "El estado es obligatorio")
    @Size(max = 20, message = "El estado no puede tener más de 20 caracteres")
    private String estado;

    @NotNull(message = "El piso es obligatorio")
    private Integer piso;

    // ---- Ubicación opcional dentro de un plano ----
    private Long planoId;
    private Integer fila;
    private Integer columna;
    private Double posX;
    private Double posY;
    private Double ancho;
    private Double alto;
}
