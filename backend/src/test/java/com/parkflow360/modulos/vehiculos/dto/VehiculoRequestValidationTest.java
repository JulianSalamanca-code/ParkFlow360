package com.parkflow360.modulos.vehiculos.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VehiculoRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testPlacaValidaFormatoAAA000() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-123")
                .tipo("VEHICULO")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Placa válida ABC-123 debería ser aceptada");
    }

    @Test
    void testPlacaValidaFormatoAAA00A() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("XYZ-00A")
                .tipo("MOTO")
                .color("Rojo")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Placa válida XYZ-00A debería ser aceptada");
    }

    @Test
    void testPlacaInvalidaSinGuion() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC123")
                .tipo("VEHICULO")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Placa sin guión ABC123 debería ser rechazada");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("formato AAA-000")));
    }

    @Test
    void testPlacaInvalidaDemasiadosCaracteres() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-1234")
                .tipo("VEHICULO")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Placa con 8 caracteres ABC-1234 debería ser rechazada");
    }

    @Test
    void testPlacaInvalidaPocosCaracteres() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("AB-123")
                .tipo("VEHICULO")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Placa con 6 caracteres AB-123 debería ser rechazada");
    }

    @Test
    void testTipoValidoVehiculo() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-123")
                .tipo("VEHICULO")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Tipo VEHICULO debería ser aceptado");
    }

    @Test
    void testTipoValidoMoto() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-123")
                .tipo("MOTO")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Tipo MOTO debería ser aceptado");
    }

    @Test
    void testTipoValidoVehiculoElectrico() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-123")
                .tipo("VEHICULO_ELECTRICO")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Tipo VEHICULO_ELECTRICO debería ser aceptado");
    }

    @Test
    void testTipoInvalido() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-123")
                .tipo("CAMION")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Tipo CAMION debería ser rechazado");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("VEHICULO, MOTO o VEHICULO_ELECTRICO")));
    }

    @Test
    void testTipoInvalidoBicicleta() {
        VehiculoRequest request = VehiculoRequest.builder()
                .placa("ABC-123")
                .tipo("BICICLETA")
                .color("Azul")
                .modelo("Test")
                .build();

        Set<ConstraintViolation<VehiculoRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Tipo BICICLETA debería ser rechazado");
    }
}
