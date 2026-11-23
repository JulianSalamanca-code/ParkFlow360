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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;
    private final WompiService wompiService;

    @PostMapping("/online")
    public ResponseEntity<PagoOnlineResponse> crearPagoOnline(
            Authentication auth,
            @Valid @RequestBody PagoOnlineRequest request) {
        return new ResponseEntity<>(wompiService.crearPagoOnline(auth.getName(), request), HttpStatus.CREATED);
    }

    @GetMapping("/mios")
    public ResponseEntity<List<PagoResponse>> misPagos(Authentication auth) {
        return ResponseEntity.ok(pagoService.listarPorUsuarioEmail(auth.getName()));
    }

    @PostMapping("/wompi/webhook")
    public ResponseEntity<Map<String, String>> webhookWompi(@RequestBody String payload) {
        wompiService.procesarWebhook(payload);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping
    public ResponseEntity<List<PagoResponse>> listarTodos(Authentication auth) {
        return ResponseEntity.ok(pagoService.listarTodos(auth.getName()));
    }

    @GetMapping("/vehiculo/{vehiculoId}")
    public ResponseEntity<List<PagoResponse>> listarPorVehiculo(
            @PathVariable Long vehiculoId,
            Authentication auth) {
        return ResponseEntity.ok(pagoService.listarPorVehiculo(auth.getName(), vehiculoId));
    }

    @GetMapping("/espacio/{espacioId}")
    public ResponseEntity<List<PagoResponse>> listarPorEspacio(
            @PathVariable Long espacioId,
            Authentication auth) {
        return ResponseEntity.ok(pagoService.listarPorEspacio(auth.getName(), espacioId));
    }

    @GetMapping("/fecha")
    public ResponseEntity<List<PagoResponse>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            Authentication auth) {
        return ResponseEntity.ok(pagoService.listarPorFecha(auth.getName(), inicio, fin));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(pagoService.obtenerPorId(auth.getName(), id));
    }

    @PostMapping
    public ResponseEntity<PagoResponse> crear(
            Authentication auth,
            @Valid @RequestBody PagoRequest request) {
        return new ResponseEntity<>(pagoService.crear(auth.getName(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoResponse> actualizar(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody PagoRequest request) {
        return ResponseEntity.ok(pagoService.actualizar(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        pagoService.eliminar(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
