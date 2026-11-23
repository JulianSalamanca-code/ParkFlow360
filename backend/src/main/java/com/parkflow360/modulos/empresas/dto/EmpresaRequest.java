package com.parkflow360.modulos.empresas.dto;

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
public class EmpresaRequest {

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(max = 150, message = "El nombre no puede tener más de 150 caracteres")
    private String nombre;

    @NotBlank(message = "El identificador de la empresa es obligatorio")
    @Size(max = 80, message = "El identificador no puede tener más de 80 caracteres")
    private String identificador;

    @NotBlank(message = "El email del usuario EMPRESA es obligatorio")
    @Email(message = "El email del usuario EMPRESA debe ser válido")
    private String usuarioEmail;
}
