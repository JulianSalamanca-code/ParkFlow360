package com.parkflow360.modulos.parqueaderos.controller;

import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoResponse;
import com.parkflow360.modulos.parqueaderos.service.ParqueaderoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
public class ParqueaderoEmpresaController {

    private final ParqueaderoService parqueaderoService;

    /** Crea el parqueadero y su usuario de pagos en la misma transacción. */
    @PostMapping("/{empresaId}/parqueaderos")
    public ResponseEntity<ParqueaderoResponse> crear(
            @PathVariable Long empresaId,
            Authentication auth,
            @Valid @RequestBody ParqueaderoRequest request) {
        return new ResponseEntity<>(
                parqueaderoService.crear(empresaId, request, auth.getName()),
                HttpStatus.CREATED);
    }
}
