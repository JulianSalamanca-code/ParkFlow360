package com.parkflow360.modulos.creditos.controller;

import com.parkflow360.modulos.creditos.dto.CreditoRequest;
import com.parkflow360.modulos.creditos.dto.CreditoResponse;
import com.parkflow360.modulos.creditos.service.CreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @GetMapping("/mios")
    public ResponseEntity<List<CreditoResponse>> misCreditos(Authentication auth) {
        return ResponseEntity.ok(creditoService.listarMios(auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<CreditoResponse>> listarTodos(Authentication auth) {
        return ResponseEntity.ok(creditoService.listarTodos(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<CreditoResponse> otorgar(
            Authentication auth,
            @Valid @RequestBody CreditoRequest request) {
        return new ResponseEntity<>(creditoService.otorgar(auth.getName(), request), HttpStatus.CREATED);
    }
}
