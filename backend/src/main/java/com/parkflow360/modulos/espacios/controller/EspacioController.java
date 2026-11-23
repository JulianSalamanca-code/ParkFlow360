package com.parkflow360.modulos.espacios.controller;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.service.EspacioService;
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
@RequestMapping("/api/espacios")
@RequiredArgsConstructor
public class EspacioController {

    private final EspacioService espacioService;

    @GetMapping
    public ResponseEntity<List<EspacioResponse>> listarTodos(Authentication auth) {
        return ResponseEntity.ok(espacioService.listarTodos(auth.getName()));
    }

    @PostMapping("/importar")
    public ResponseEntity<List<EspacioResponse>> importar(
            Authentication auth,
            @Valid @RequestBody List<@Valid EspacioRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(espacioService.importar(auth.getName(), requests));
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<EspacioResponse>> listarPorEstado(
            @PathVariable String estado,
            Authentication auth) {
        return ResponseEntity.ok(espacioService.listarPorEstado(auth.getName(), estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspacioResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(espacioService.obtenerPorId(auth.getName(), id));
    }

    @PostMapping
    public ResponseEntity<EspacioResponse> crear(
            Authentication auth,
            @Valid @RequestBody EspacioRequest request) {
        return new ResponseEntity<>(espacioService.crear(auth.getName(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspacioResponse> actualizar(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody EspacioRequest request) {
        return ResponseEntity.ok(espacioService.actualizar(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        espacioService.eliminar(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
