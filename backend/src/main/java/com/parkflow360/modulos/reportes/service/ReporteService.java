package com.parkflow360.modulos.reportes.service;

import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import com.parkflow360.modulos.reportes.dto.ReporteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VehiculoRepository vehiculoRepository;
    private final EspacioRepository espacioRepository;
    private final TarifaRepository tarifaRepository;
    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public List<ReporteResponse> generarReporteGeneral() {
        return List.of(
            ReporteResponse.builder()
                .tipo("VEHICULOS")
                .cantidad(vehiculoRepository.count())
                .total(BigDecimal.ZERO)
                .descripcion("Total de vehículos registrados")
                .build(),
            ReporteResponse.builder()
                .tipo("ESPACIOS")
                .cantidad(espacioRepository.count())
                .total(BigDecimal.ZERO)
                .descripcion("Total de espacios registrados")
                .build(),
            ReporteResponse.builder()
                .tipo("TARIFAS")
                .cantidad(tarifaRepository.count())
                .total(BigDecimal.ZERO)
                .descripcion("Total de tarifas registradas")
                .build(),
            ReporteResponse.builder()
                .tipo("PAGOS")
                .cantidad(pagoRepository.count())
                .total(pagoRepository.findAll().stream()
                    .map(p -> p.getValor())
                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                .descripcion("Total de pagos registrados")
                .build()
        );
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> generarReporteEspaciosPorEstado() {
        return List.of("LIBRE", "OCUPADO", "RESERVADO", "MANTENIMIENTO").stream()
            .map(estado -> {
                long count = espacioRepository.findByEstado(estado).size();
                return ReporteResponse.builder()
                    .tipo("ESPACIO_" + estado)
                    .cantidad(count)
                    .total(BigDecimal.ZERO)
                    .descripcion("Espacios en estado " + estado)
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> generarReporteIngresosPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        return pagoRepository.findByFechaBetween(inicio, fin).stream()
            .collect(Collectors.groupingBy(
                p -> p.getMetodoPago() != null ? p.getMetodoPago() : "SIN_METODO",
                Collectors.reducing(BigDecimal.ZERO, p -> p.getValor(), BigDecimal::add)
            ))
            .entrySet().stream()
            .map(entry -> ReporteResponse.builder()
                .tipo("INGRESOS_" + entry.getKey())
                .cantidad((long) pagoRepository.findByFechaBetween(inicio, fin).size())
                .total(entry.getValue())
                .descripcion("Ingresos por " + entry.getKey())
                .build())
            .collect(Collectors.toList());
    }
}
