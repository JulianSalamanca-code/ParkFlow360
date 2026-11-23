package com.parkflow360.modulos.parqueaderos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoVehiculoResponse {

    private Long id;
    private String tipo;
    private boolean cantidadFija;
    private Integer cantidad;
    private Long espaciosOcupados;
}
