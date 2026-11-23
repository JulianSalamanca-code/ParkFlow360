package com.parkflow360.modulos.parqueaderos.dto;

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
public class VinculacionRequest {

    @NotBlank(message = "El identificador del parqueadero es obligatorio")
    @Size(max = 80, message = "El identificador no puede tener más de 80 caracteres")
    private String identificador;
}
