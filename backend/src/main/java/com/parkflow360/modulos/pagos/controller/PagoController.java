package com.parkflow360.modulos.pagos.controller;

import com.parkflow360.modulos.pagos.dto.PagoOnlineRequest;
import com.parkflow360.modulos.pagos.dto.PagoOnlineResponse;
import com.parkflow360.modulos.pagos.dto.PagoRequest;
import com.parkflow360.modulos.pagos.dto.PagoResponse;
import com.parkflow360.modulos.pagos.service.PagoService;
import com.parkflow360.modulos.pagos.service.WompiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final WompiService wompiService;

    // ---------- Cliente ----------

    /** Inicia un pago en línea y devuelve la URL de checkout de Wompi. */
    @PostMapping("/online")
    public ResponseEntity<PagoOnlineResponse> crearPagoOnline(
            Authentication auth,
            @Valid @RequestBody PagoOnlineRequest request) {
        return new ResponseEntity<>(wompiService.crearPagoOnline(auth.getName(), request), HttpStatus.CREATED);
    }

    /** Historial de pagos del cliente autenticado. */
    @GetMapping("/mios")
    public ResponseEntity<List<PagoResponse>> misPagos(Authentication auth) {
        return ResponseEntity.ok(pagoService.listarPorUsuarioEmail(auth.getName()));
    }

    /** Webhook público de Wompi: confirma el resultado de la transacción. */
    @PostMapping("/wompi/webhook")
    public ResponseEntity<Map<String, String>> webhookWompi(@RequestBody String payload) {
        wompiService.procesarWebhook(payload);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    // ---------- Administrador ----------

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<PagoResponse>> listarPorVehiculo(@PathVariable Long vehiculoId) {
        return ResponseEntity.ok(pagoService.listarPorVehiculo(vehiculoId));
    }

    @GetMapping("/espacio/{espacioId}")
    public ResponseEntity<List<PagoResponse>> listarPorEspacio(@PathVariable Long espacioId) {
        return ResponseEntity.ok(pagoService.listarPorEspacio(espacioId));
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<PagoResponse>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(pagoService.listarPorFecha(inicio, fin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PagoResponse> crear(@Valid @RequestBody PagoRequest request) {
        return new ResponseEntity<>(pagoService.crear(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pagoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
