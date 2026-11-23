package com.parkflow360.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "clave-secreta-de-prueba-con-mas-de-32-caracteres");
        ReflectionTestUtils.setField(jwtService, "expiration", 3_600_000L);
    }

    @Test
    void tokenConAdminGeneraClaimPlataforma() {
        String token = jwtService.generateToken("admin@test.com", "ADMIN", "Administrador");

        assertEquals("PLATAFORMA", jwtService.extractRol(token));
        assertEquals("admin@test.com", jwtService.extractEmail(token));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void tokenConRolVigenteConservaElRol() {
        String token = jwtService.generateToken("guardia@test.com", "guardia", "Guardia");

        assertEquals("GUARDIA", jwtService.extractRol(token));
    }
}
