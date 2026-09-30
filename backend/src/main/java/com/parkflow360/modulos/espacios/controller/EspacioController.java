package com.parkflow360.modulos.espacios.controller;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.service.EspacioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
public class EspacioController {

    private final EspacioService espacioService;

    @GetMapping
    public ResponseEntity<List<EspacioResponse>> listarTodos() {
        return ResponseEntity.ok(espacioService.listarTodos());
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<EspacioResponse>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(espacioService.listarPorEstado(estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(espacioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<EspacioResponse> crear(@Valid @RequestBody EspacioRequest request) {
        return new ResponseEntity<>(espacioService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspacioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EspacioRequest request) {
        return ResponseEntity.ok(espacioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        espacioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
