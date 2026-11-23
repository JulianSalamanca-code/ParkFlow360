package com.parkflow360.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleLoginRequest {

    @NotBlank(message = "El credential de Google es obligatorio")
    private String credential;
}
