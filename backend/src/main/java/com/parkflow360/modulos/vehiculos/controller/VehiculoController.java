package com.parkflow360.modulos.vehiculos.controller;

import com.parkflow360.modulos.vehiculos.dto.VehiculoRequest;
import com.parkflow360.modulos.vehiculos.dto.VehiculoResponse;
import com.parkflow360.modulos.vehiculos.service.VehiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoService vehiculoService;

    // ---------- Usuario (cliente): solo sus vehículos ----------

    @GetMapping("/mios")
    public ResponseEntity<List<VehiculoResponse>> misVehiculos(Authentication auth) {
        return ResponseEntity.ok(vehiculoService.listarPorUsuarioEmail(auth.getName()));
    }

    @PostMapping("/mios")
    public ResponseEntity<VehiculoResponse> crearMiVehiculo(
            Authentication auth,
            @Valid @RequestBody VehiculoRequest request) {
        return new ResponseEntity<>(vehiculoService.crearParaUsuario(auth.getName(), request), HttpStatus.CREATED);
    }

    @PutMapping("/mios/{id}")
    public ResponseEntity<VehiculoResponse> actualizarMiVehiculo(
            Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.ok(vehiculoService.actualizarParaUsuario(auth.getName(), id, request));
    }

    @DeleteMapping("/mios/{id}")
    public ResponseEntity<Void> eliminarMiVehiculo(Authentication auth, @PathVariable Long id) {
        vehiculoService.eliminarParaUsuario(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }

    // ---------- Administrador ----------

    @GetMapping
    public ResponseEntity<List<VehiculoResponse>> listarTodos() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoResponse> crear(@Valid @RequestBody VehiculoRequest request) {
        return new ResponseEntity<>(vehiculoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody VehiculoRequest request) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        vehiculoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
