package com.parkflow360.modulos.reportes.service;

import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.reportes.dto.ReporteResponse;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.modulos.vehiculos.entity.Vehiculo;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final VehiculoRepository vehiculoRepository;
    private final EspacioRepository espacioRepository;
    private final TarifaRepository tarifaRepository;
    private final PagoRepository pagoRepository;
    private final IngresoRepository ingresoRepository;
    private final AccesoParqueaderoService accesoParqueaderoService;

    @Transactional(readOnly = true)
    public List<ReporteResponse> generarReporteGeneral(String email) {
        boolean plataforma = accesoParqueaderoService.esPlataforma(email);
        List<Long> permitidos = plataforma
                ? List.of()
                : accesoParqueaderoService.parqueaderosPermitidos(email);

        long vehiculos;
        long espacios;
        long tarifas;
        List<Pago> pagos;

        if (plataforma) {
            vehiculos = vehiculoRepository.count();
            espacios = espacioRepository.count();
            tarifas = tarifaRepository.count();
            pagos = pagoRepository.findAll();
        } else {
            Set<Long> vehiculoIds = new HashSet<>();
            Set<Long> usuarioIds = new HashSet<>();
            for (Ingreso ingreso : ingresoRepository.findByParqueaderoIdIn(permitidos)) {
                if (ingreso.getVehiculoId() != null) {
                    vehiculoIds.add(ingreso.getVehiculoId());
                }
                if (ingreso.getUsuarioId() != null) {
                    usuarioIds.add(ingreso.getUsuarioId());
                }
            }

            pagos = pagoRepository.findByParqueaderoIdIn(permitidos);
            for (Pago pago : pagos) {
                if (pago.getVehiculoId() != null) {
                    vehiculoIds.add(pago.getVehiculoId());
                }
                if (pago.getUsuarioId() != null) {
                    usuarioIds.add(pago.getUsuarioId());
                }
            }

            Set<Vehiculo> vehiculosPermitidos = new HashSet<>();
            if (!vehiculoIds.isEmpty()) {
                vehiculosPermitidos.addAll(vehiculoRepository.findAllById(vehiculoIds));
            }
            if (!usuarioIds.isEmpty()) {
                vehiculosPermitidos.addAll(vehiculoRepository.findByUsuarioIdIn(usuarioIds));
            }
            vehiculos = vehiculosPermitidos.size();
            espacios = espacioRepository.findByParqueaderoIdIn(permitidos).size();
            tarifas = tarifaRepository
                    .findByParqueaderoIdIsNullOrParqueaderoIdIn(permitidos).size();
        }

        return List.of(
            ReporteResponse.builder()
                .tipo("VEHICULOS")
                .cantidad(vehiculos)
                .total(BigDecimal.ZERO)
                .descripcion("Total de vehículos registrados")
                .build(),
            ReporteResponse.builder()
                .tipo("ESPACIOS")
                .cantidad(espacios)
                .total(BigDecimal.ZERO)
                .descripcion("Total de espacios registrados")
                .build(),
            ReporteResponse.builder()
                .tipo("TARIFAS")
                .cantidad(tarifas)
                .total(BigDecimal.ZERO)
                .descripcion("Total de tarifas registradas")
                .build(),
            ReporteResponse.builder()
                .tipo("PAGOS")
                .cantidad((long) pagos.size())
                .total(pagos.stream()
                    .map(Pago::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add))
                .descripcion("Total de pagos registrados")
                .build()
        );
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> generarReporteEspaciosPorEstado(String email) {
        Collection<Espacio> espacios = espaciosVisibles(email);
        return List.of("LIBRE", "OCUPADO", "RESERVADO", "MANTENIMIENTO").stream()
            .map(estado -> {
                long count = espacios.stream()
                        .filter(espacio -> estado.equalsIgnoreCase(espacio.getEstado()))
                        .count();
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
    public List<ReporteResponse> generarReporteIngresosPorPeriodo(
            String email, LocalDateTime inicio, LocalDateTime fin) {
        List<Pago> pagos = pagosVisibles(email).stream()
            .filter(pago -> pago.getFecha() != null
                    && !pago.getFecha().isBefore(inicio)
                    && !pago.getFecha().isAfter(fin))
            .collect(Collectors.toList());

        Map<String, List<Pago>> porMetodo = pagos.stream().collect(Collectors.groupingBy(
            pago -> pago.getMetodoPago() != null ? pago.getMetodoPago() : "SIN_METODO",
            LinkedHashMap::new,
            Collectors.toList()
        ));

        return porMetodo.entrySet().stream()
            .map(entry -> ReporteResponse.builder()
                .tipo("INGRESOS_" + entry.getKey())
                .cantidad((long) entry.getValue().size())
                .total(entry.getValue().stream()
                        .map(Pago::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .descripcion("Ingresos por " + entry.getKey())
                .build())
            .collect(Collectors.toList());
    }

    private Collection<Espacio> espaciosVisibles(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return espacioRepository.findAll();
        }
        return espacioRepository.findByParqueaderoIdIn(
                accesoParqueaderoService.parqueaderosPermitidos(email));
    }

    private Collection<Pago> pagosVisibles(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return pagoRepository.findAll();
        }
        return pagoRepository.findByParqueaderoIdIn(
                accesoParqueaderoService.parqueaderosPermitidos(email));
    }
}
