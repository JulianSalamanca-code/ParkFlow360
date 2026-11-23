package com.parkflow360.modulos.parqueaderos.dto;

import jakarta.validation.constraints.Email;
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
public class GuardiaRequest {

    @NotBlank(message = "El email del guardia es obligatorio")
    @Email(message = "El email del guardia debe ser válido")
    private String email;

    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
