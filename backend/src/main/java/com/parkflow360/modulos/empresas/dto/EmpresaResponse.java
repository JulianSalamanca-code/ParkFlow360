package com.parkflow360.modulos.empresas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpresaResponse {

    private Long id;
    private String nombre;
    private String identificador;
    private boolean activo;
    private LocalDateTime fechaCreacion;
}
