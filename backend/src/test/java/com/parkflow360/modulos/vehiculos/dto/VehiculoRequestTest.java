package com.parkflow360.modulos.vehiculos.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VehiculoRequestTest {

    private static Validator validator;

    @BeforeAll
    static void crearValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void normalizaYValidaLaPlacaConGuion() {
        VehiculoRequest request = VehiculoRequest.builder()
                .tipo("MOTO")
                .build();
        request.setPlaca(" cux-461 ");

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
        assertEquals("CUX-461", request.getPlaca());
    }

    @Test
    void rechazaPlacaSinGuion() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("CUX461")
                .tipo("MOTO")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("placa")));
    }
}
