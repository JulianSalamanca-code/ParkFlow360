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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planos")
@RequiredArgsConstructor
public class PlanoController {

    private final PlanoService planoService;

    @GetMapping
    public ResponseEntity<List<PlanoResponse>> listarTodos() {
        return ResponseEntity.ok(planoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(planoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PlanoResponse> crear(@Valid @RequestBody PlanoRequest request) {
        return new ResponseEntity<>(planoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PlanoRequest request) {
        return ResponseEntity.ok(planoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        planoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** Genera en bloque los espacios del plano (cuadrícula). */
    @PostMapping("/{id}/generar-espacios")
    public ResponseEntity<List<EspacioResponse>> generarEspacios(
            @PathVariable Long id,
            @Valid @RequestBody GenerarEspaciosRequest request) {
        return new ResponseEntity<>(planoService.generarEspacios(id, request), HttpStatus.CREATED);
    }

    /** Espacios ubicados en un plano (para el mapa de ocupación). */
    @GetMapping("/{id}/espacios")
    public ResponseEntity<List<EspacioResponse>> listarEspacios(@PathVariable Long id) {
        return ResponseEntity.ok(planoService.listarEspacios(id));
    }
}
