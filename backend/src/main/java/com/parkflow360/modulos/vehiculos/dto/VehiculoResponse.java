package com.parkflow360.modulos.vehiculos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoResponse {
    private Long id;
    private String placa;
    private String tipo;
    private String color;
    private String modelo;
    private LocalDateTime fechaCreacion;
}
