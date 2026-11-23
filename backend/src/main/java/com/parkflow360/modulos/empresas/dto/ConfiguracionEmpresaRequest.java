package com.parkflow360.modulos.empresas.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfiguracionEmpresaRequest {

    @NotNull(message = "La tolerancia de salida es obligatoria")
    @Min(value = 0, message = "La tolerancia de salida no puede ser negativa")
    @Max(value = 240, message = "La tolerancia de salida no puede superar 240 minutos")
    private Integer toleranciaSalidaMinutos;
}
