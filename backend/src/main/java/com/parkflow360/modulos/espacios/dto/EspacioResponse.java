package com.parkflow360.modulos.espacios.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EspacioResponse {
    private Long id;
    private String numero;
    private String tipo;
    private String estado;
    private Integer piso;
    private LocalDateTime fechaCreacion;
}
