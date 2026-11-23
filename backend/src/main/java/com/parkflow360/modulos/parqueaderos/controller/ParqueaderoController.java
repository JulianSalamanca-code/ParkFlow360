package com.parkflow360.modulos.parqueaderos.controller;

import com.parkflow360.modulos.parqueaderos.dto.GuardiaRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoResponse;
import com.parkflow360.modulos.parqueaderos.dto.TipoVehiculoResponse;
import com.parkflow360.modulos.parqueaderos.dto.VinculacionRequest;
import com.parkflow360.modulos.parqueaderos.service.ParqueaderoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/parqueaderos")
@RequiredArgsConstructor
public class ParqueaderoController {

    private final ParqueaderoService parqueaderoService;

    @PostMapping("/vincular")
    public ResponseEntity<ParqueaderoResponse> vincular(
            Authentication auth,
            @Valid @RequestBody VinculacionRequest request) {
        return ResponseEntity.ok(parqueaderoService.vincular(auth.getName(), request));
    }

    @GetMapping("/mios")
    public ResponseEntity<List<ParqueaderoResponse>> listarMios(Authentication auth) {
        return ResponseEntity.ok(parqueaderoService.listarMios(auth.getName()));
    }

    @PostMapping("/{id}/guardias")
    public ResponseEntity<Void> asignarGuardia(
            @PathVariable Long id,
            Authentication auth,
            @Valid @RequestBody GuardiaRequest request) {
        parqueaderoService.asignarGuardia(id, request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}/tipos")
    public ResponseEntity<List<TipoVehiculoResponse>> listarTipos(
            @PathVariable Long id,
            Authentication auth) {
        return ResponseEntity.ok(parqueaderoService.listarTipos(auth.getName(), id));
    }
}
