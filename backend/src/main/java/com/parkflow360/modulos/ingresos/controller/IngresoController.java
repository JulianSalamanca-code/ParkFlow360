package com.parkflow360.modulos.ingresos.controller;

import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.service.IngresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;

    // ---------- Usuario (cliente): solo sus parqueos ----------

    @GetMapping("/mios")
    public ResponseEntity<List<IngresoResponse>> misIngresos(Authentication auth) {
        return ResponseEntity.ok(ingresoService.listarPorUsuarioEmail(auth.getName()));
    }

    @PostMapping("/reservar")
    public ResponseEntity<IngresoResponse> reservar(
            Authentication auth,
            @Valid @RequestBody IngresoRequest request) {
        return new ResponseEntity<>(ingresoService.reservar(auth.getName(), request), HttpStatus.CREATED);
    }

    // ---------- Administrador ----------

    @GetMapping
    public ResponseEntity<List<IngresoResponse>> listarTodos() {
        return ResponseEntity.ok(ingresoService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<IngresoResponse>> listarActivos() {
        return ResponseEntity.ok(ingresoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngresoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ingresoService.obtenerPorId(id));
    }

    @GetMapping("/folio/{folio}")
    public ResponseEntity<IngresoResponse> obtenerPorFolio(@PathVariable String folio) {
        return ResponseEntity.ok(ingresoService.obtenerPorFolio(folio));
    }

    @PostMapping
    public ResponseEntity<IngresoResponse> crear(@Valid @RequestBody IngresoRequest request) {
        return new ResponseEntity<>(ingresoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/salida")
    public ResponseEntity<IngresoResponse> registrarSalida(@PathVariable Long id) {
        return ResponseEntity.ok(ingresoService.registrarSalida(id));
    }

    @PutMapping("/{id}/liberar")
    public ResponseEntity<IngresoResponse> liberar(@PathVariable Long id) {
        return ResponseEntity.ok(ingresoService.liberar(id));
    }

    @PutMapping("/espacio/{espacioId}/liberar")
    public ResponseEntity<IngresoResponse> liberarPorEspacio(@PathVariable Long espacioId) {
        return ResponseEntity.ok(ingresoService.liberarPorEspacio(espacioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ingresoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
