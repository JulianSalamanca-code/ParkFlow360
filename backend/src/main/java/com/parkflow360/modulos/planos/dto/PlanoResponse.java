package com.parkflow360.modulos.planos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanoResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private Integer piso;
    private Integer filas;
    private Integer columnas;
    private Integer ancho;
    private Integer alto;
    private long totalEspacios;
    private LocalDateTime fechaCreacion;
}
