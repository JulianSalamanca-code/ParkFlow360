package com.parkflow360.modulos.saldos.controller;

import com.parkflow360.modulos.saldos.dto.RecargaRequest;
import com.parkflow360.modulos.saldos.dto.SaldoResponse;
import com.parkflow360.modulos.saldos.service.SaldoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/saldos")
@RequiredArgsConstructor
public class SaldoController {

    private final SaldoService saldoService;

    @GetMapping("/mi-cuenta")
    public ResponseEntity<SaldoResponse> miCuenta(Authentication auth) {
        return ResponseEntity.ok(saldoService.obtenerMiCuenta(auth.getName()));
    }

    @PostMapping("/recargar")
    public ResponseEntity<SaldoResponse> recargar(
            Authentication auth,
            @Valid @RequestBody RecargaRequest request) {
        return ResponseEntity.ok(saldoService.recargar(auth.getName(), request));
    }
}
