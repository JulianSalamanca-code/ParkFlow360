package com.parkflow360.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Datos relevantes devueltos por la verificación del ID token de Google. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleUserInfo {
    private String sub;
    private String email;
    private String name;
    private boolean emailVerified;
}
