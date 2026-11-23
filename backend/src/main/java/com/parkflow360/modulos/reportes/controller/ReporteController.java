package com.parkflow360.modulos.reportes.controller;

import com.parkflow360.modulos.reportes.dto.ReporteResponse;
import com.parkflow360.modulos.reportes.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/general")
    public ResponseEntity<List<ReporteResponse>> reporteGeneral(Authentication auth) {
        return ResponseEntity.ok(reporteService.generarReporteGeneral(auth.getName()));
    }

    @GetMapping("/espacios-por-estado")
    public ResponseEntity<List<ReporteResponse>> espaciosPorEstado(Authentication auth) {
        return ResponseEntity.ok(reporteService.generarReporteEspaciosPorEstado(auth.getName()));
    }

    @GetMapping("/ingresos-por-periodo")
    public ResponseEntity<List<ReporteResponse>> ingresosPorPeriodo(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(
                reporteService.generarReporteIngresosPorPeriodo(auth.getName(), inicio, fin));
    }
}
