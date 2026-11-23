package com.parkflow360.modulos.parqueaderos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParqueaderoRequest {

    @NotBlank(message = "El nombre del parqueadero es obligatorio")
    @Size(max = 150, message = "El nombre no puede tener más de 150 caracteres")
    private String nombre;

    @NotBlank(message = "El identificador del parqueadero es obligatorio")
    @Size(max = 80, message = "El identificador no puede tener más de 80 caracteres")
    private String identificador;

    @PositiveOrZero(message = "La capacidad de vehículos no puede ser negativa")
    private Integer capacidadVehiculos;

    @Valid
    private List<TipoVehiculoRequest> tipos;

    @NotBlank(message = "El email del cajero es obligatorio")
    @Email(message = "El email del cajero debe ser válido")
    private String cajeroEmail;

    @NotBlank(message = "La contraseña temporal del cajero es obligatoria")
    @Size(min = 8, message = "La contraseña temporal debe tener al menos 8 caracteres")
    private String cajeroPassword;

    @Size(max = 100, message = "El nombre del cajero no puede tener más de 100 caracteres")
    private String cajeroNombre;
}
