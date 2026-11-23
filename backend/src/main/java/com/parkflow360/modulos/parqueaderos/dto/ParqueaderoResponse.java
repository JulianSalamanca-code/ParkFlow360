package com.parkflow360.modulos.parqueaderos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParqueaderoResponse {

    private Long id;
    private Long empresaId;
    private String nombre;
    private String identificador;
    private Integer capacidadVehiculos;
    private boolean activo;
    private String cajeroEmail;
    private List<TipoVehiculoResponse> tipos;
    private LocalDateTime fechaCreacion;
}
