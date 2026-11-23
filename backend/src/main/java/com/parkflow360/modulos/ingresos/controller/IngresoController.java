package com.parkflow360.modulos.ingresos.controller;

import com.parkflow360.modulos.ingresos.dto.EstadoPagoResponse;
import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.dto.VerificarIngresoRequest;
import com.parkflow360.modulos.ingresos.service.EstadoPagoService;
import com.parkflow360.modulos.ingresos.service.IngresoService;
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
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;
    private final EstadoPagoService estadoPagoService;

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

    @GetMapping
    public ResponseEntity<List<IngresoResponse>> listarTodos(Authentication auth) {
        return ResponseEntity.ok(ingresoService.listarTodos(auth.getName()));
    }

    @GetMapping("/activos")
    public ResponseEntity<List<IngresoResponse>> listarActivos(Authentication auth) {
        return ResponseEntity.ok(ingresoService.listarActivos(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngresoResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(ingresoService.obtenerPorId(auth.getName(), id));
    }

    @GetMapping("/folio/{folio}")
    public ResponseEntity<IngresoResponse> obtenerPorFolio(
            @PathVariable String folio,
            Authentication auth) {
        return ResponseEntity.ok(ingresoService.obtenerPorFolio(auth.getName(), folio));
    }

    @PostMapping
    public ResponseEntity<IngresoResponse> crear(
            Authentication auth,
            @Valid @RequestBody IngresoRequest request) {
        return new ResponseEntity<>(ingresoService.crear(auth.getName(), request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/salida")
    public ResponseEntity<IngresoResponse> registrarSalida(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(ingresoService.registrarSalida(auth.getName(), id));
    }

    @PutMapping("/{id}/llegada")
    public ResponseEntity<IngresoResponse> registrarLlegada(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(ingresoService.registrarLlegada(auth.getName(), id));
    }

    @PutMapping("/{id}/verificar")
    public ResponseEntity<IngresoResponse> verificar(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody VerificarIngresoRequest request) {
        return ResponseEntity.ok(ingresoService.verificar(auth.getName(), id, request));
    }

    @GetMapping("/{id}/estado-pago")
    public ResponseEntity<EstadoPagoResponse> estadoPago(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(estadoPagoService.obtener(auth.getName(), id));
    }

    @PutMapping("/{id}/liberar")
    public ResponseEntity<IngresoResponse> liberar(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(ingresoService.liberar(auth.getName(), id));
    }

    @PutMapping("/espacio/{espacioId}/liberar")
    public ResponseEntity<IngresoResponse> liberarPorEspacio(
            @PathVariable Long espacioId,
            Authentication auth) {
        return ResponseEntity.ok(ingresoService.liberarPorEspacio(auth.getName(), espacioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Authentication auth) {
        ingresoService.eliminar(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
