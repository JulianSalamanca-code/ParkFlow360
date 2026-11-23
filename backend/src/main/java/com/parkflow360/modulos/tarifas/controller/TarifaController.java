package com.parkflow360.modulos.tarifas.controller;

import com.parkflow360.modulos.tarifas.dto.TarifaRequest;
import com.parkflow360.modulos.tarifas.dto.TarifaResponse;
import com.parkflow360.modulos.tarifas.service.TarifaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService tarifaService;

    @GetMapping
    public ResponseEntity<List<TarifaResponse>> listarTodos(Authentication auth) {
        return ResponseEntity.ok(tarifaService.listarTodos(auth.getName()));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TarifaResponse>> listarPorTipo(
            @PathVariable String tipo,
            Authentication auth) {
        return ResponseEntity.ok(tarifaService.listarPorTipo(auth.getName(), tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(tarifaService.obtenerPorId(auth.getName(), id));
    }

    @PostMapping
    public ResponseEntity<TarifaResponse> crear(
            Authentication auth,
            @Valid @RequestBody TarifaRequest request) {
        return new ResponseEntity<>(tarifaService.crear(auth.getName(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifaResponse> actualizar(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody TarifaRequest request) {
        return ResponseEntity.ok(tarifaService.actualizar(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        tarifaService.eliminar(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
