package com.parkflow360.modulos.tarifas.controller;

import com.parkflow360.modulos.tarifas.dto.TarifaRequest;
import com.parkflow360.modulos.tarifas.dto.TarifaResponse;
import com.parkflow360.modulos.tarifas.service.TarifaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService tarifaService;

    @GetMapping
    public ResponseEntity<List<TarifaResponse>> listarTodos() {
        return ResponseEntity.ok(tarifaService.listarTodos());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<TarifaResponse>> listarPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(tarifaService.listarPorTipo(tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarifaResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarifaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<TarifaResponse> crear(@Valid @RequestBody TarifaRequest request) {
        return new ResponseEntity<>(tarifaService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TarifaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarifaRequest request) {
        return ResponseEntity.ok(tarifaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tarifaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
