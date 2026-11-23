package com.parkflow360.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolesTest {

    @Test
    void adminSeNormalizaAPlataforma() {
        assertEquals(Roles.PLATAFORMA, Roles.normalizar("ADMIN"));
        assertEquals(Roles.PLATAFORMA, Roles.normalizar(" admin "));
        assertTrue(Roles.esPlataforma("ADMIN"));
    }

    @Test
    void rolesVigentesSonValidos() {
        assertTrue(Roles.esValido("PLATAFORMA"));
        assertTrue(Roles.esValido("EMPRESA"));
        assertTrue(Roles.esValido("CAJERO_PAGOS"));
        assertTrue(Roles.esValido("GUARDIA"));
        assertTrue(Roles.esValido("USUARIO"));
        assertFalse(Roles.esValido("OPERADOR"));
    }
}
