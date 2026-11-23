package com.parkflow360.security;

import java.util.Locale;
import java.util.Set;

/** Roles vigentes y alias transitorio ADMIN para no invalidar tokens antiguos. */
public final class Roles {

    public static final String PLATAFORMA = "PLATAFORMA";
    public static final String EMPRESA = "EMPRESA";
    public static final String CAJERO_PAGOS = "CAJERO_PAGOS";
    public static final String GUARDIA = "GUARDIA";
    public static final String USUARIO = "USUARIO";
    public static final String ADMIN_LEGADO = "ADMIN";

    private static final Set<String> VIGENTES = Set.of(
            PLATAFORMA, EMPRESA, CAJERO_PAGOS, GUARDIA, USUARIO);

    private Roles() {
    }

    public static String normalizar(String rol) {
        if (rol == null || rol.isBlank()) {
            return USUARIO;
        }
        String normalizado = rol.trim().toUpperCase(Locale.ROOT);
        return ADMIN_LEGADO.equals(normalizado) ? PLATAFORMA : normalizado;
    }

    public static boolean esValido(String rol) {
        return VIGENTES.contains(normalizar(rol));
    }

    public static boolean esPlataforma(String rol) {
        return PLATAFORMA.equals(normalizar(rol));
    }
}
