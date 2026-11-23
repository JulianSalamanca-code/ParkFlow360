package com.parkflow360.modulos.pagos.controller;

import com.parkflow360.modulos.pagos.dto.FacturaResponse;
import com.parkflow360.modulos.pagos.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping("/mios")
    public ResponseEntity<List<FacturaResponse>> misFacturas(Authentication auth) {
        return ResponseEntity.ok(facturaService.listarMias(auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponse>> listarTodas(Authentication auth) {
        return ResponseEntity.ok(facturaService.listarTodas(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponse> obtenerPorId(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(facturaService.obtenerPorId(auth.getName(), id));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<FacturaResponse> pagar(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(facturaService.pagar(auth.getName(), id));
    }
}
