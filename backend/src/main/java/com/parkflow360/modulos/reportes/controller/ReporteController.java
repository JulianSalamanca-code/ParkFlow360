package com.parkflow360.modulos.reportes.controller;

import com.parkflow360.modulos.reportes.dto.ReporteResponse;
import com.parkflow360.modulos.reportes.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/general")
    public ResponseEntity<List<ReporteResponse>> reporteGeneral() {
        return ResponseEntity.ok(reporteService.generarReporteGeneral());
    }

    @GetMapping("/espacios-por-estado")
    public ResponseEntity<List<ReporteResponse>> espaciosPorEstado() {
        return ResponseEntity.ok(reporteService.generarReporteEspaciosPorEstado());
    }

    @GetMapping("/ingresos-por-periodo")
    public ResponseEntity<List<ReporteResponse>> ingresosPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(reporteService.generarReporteIngresosPorPeriodo(inicio, fin));
    }
}
