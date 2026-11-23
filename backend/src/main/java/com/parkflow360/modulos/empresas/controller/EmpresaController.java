package com.parkflow360.modulos.empresas.controller;

import com.parkflow360.modulos.empresas.dto.ConfiguracionEmpresaRequest;
import com.parkflow360.modulos.empresas.dto.EmpresaRequest;
import com.parkflow360.modulos.empresas.dto.EmpresaResponse;
import com.parkflow360.modulos.empresas.service.EmpresaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    public ResponseEntity<EmpresaResponse> crear(
            Authentication auth,
            @Valid @RequestBody EmpresaRequest request) {
        return new ResponseEntity<>(empresaService.crear(auth.getName(), request), HttpStatus.CREATED);
    }

    @GetMapping("/mias")
    public ResponseEntity<List<EmpresaResponse>> listarMias(Authentication auth) {
        return ResponseEntity.ok(empresaService.listarMias(auth.getName()));
    }

    @PutMapping("/{id}/configuracion")
    public ResponseEntity<EmpresaResponse> actualizarConfiguracion(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody ConfiguracionEmpresaRequest request) {
        return ResponseEntity.ok(
                empresaService.actualizarConfiguracion(auth.getName(), id, request));
    }
}
