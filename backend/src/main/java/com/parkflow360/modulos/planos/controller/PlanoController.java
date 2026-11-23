package com.parkflow360.modulos.planos.controller;

import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.planos.dto.GenerarEspaciosRequest;
import com.parkflow360.modulos.planos.dto.PlanoRequest;
import com.parkflow360.modulos.planos.dto.PlanoResponse;
import com.parkflow360.modulos.planos.service.PlanoService;
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
@RequestMapping("/api/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;

    @GetMapping
    public ResponseEntity<List<PlanoResponse>> listarTodos(Authentication auth) {
        return ResponseEntity.ok(planoService.listarTodos(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(planoService.obtenerPorId(auth.getName(), id));
    }

    @PostMapping
    public ResponseEntity<PlanoResponse> crear(
            Authentication auth,
            @Valid @RequestBody PlanoRequest request) {
        return new ResponseEntity<>(planoService.crear(auth.getName(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoResponse> actualizar(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody PlanoRequest request) {
        return ResponseEntity.ok(planoService.actualizar(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        planoService.eliminar(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/generar-espacios")
    public ResponseEntity<List<EspacioResponse>> generarEspacios(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody GenerarEspaciosRequest request) {
        return new ResponseEntity<>(
                planoService.generarEspacios(auth.getName(), id, request),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}/espacios")
    public ResponseEntity<List<EspacioResponse>> listarEspacios(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(planoService.listarEspacios(auth.getName(), id));
    }
}
