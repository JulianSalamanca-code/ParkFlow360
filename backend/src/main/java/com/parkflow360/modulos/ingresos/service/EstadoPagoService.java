package com.parkflow360.modulos.ingresos.service;

import com.parkflow360.modulos.creditos.entity.Credito;
import com.parkflow360.modulos.creditos.repository.CreditoRepository;
import com.parkflow360.modulos.ingresos.dto.EstadoPagoResponse;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.entity.Factura;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.FacturaRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.pagos.service.FacturaService;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import com.parkflow360.shared.service.ToleranciaSalidaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * F7: consulta si un vehículo está al día. No crea ni modifica pagos: el guardia
 * solo verifica, no gestiona cobros.
 */
@Service
@RequiredArgsConstructor
public class EstadoPagoService {

    private static final String CONFIRMADO = "CONFIRMADO";
    private static final String PAGADA = "PAGADA";
    private static final String PENDIENTE = "PENDIENTE";

    private final IngresoRepository ingresoRepository;
    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final CreditoRepository creditoRepository;
    private final AccesoParqueaderoService accesoParqueaderoService;
    private final FacturaService facturaService;
    private final ToleranciaSalidaService toleranciaSalidaService;

    @Transactional(readOnly = true)
    public EstadoPagoResponse obtener(String email, Long ingresoId) {
        Ingreso ingreso = ingresoRepository.findById(ingresoId)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + ingresoId));
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        Factura factura = facturaRepository.findByIngresoId(ingresoId).orElse(null);
        Pago pago = pagoRepository.findByIngresoId(ingresoId).stream()
                .filter(p -> CONFIRMADO.equals(p.getEstadoPago()))
                .findFirst()
                .orElse(null);
        Credito credito = factura != null
                ? creditoRepository.findByFacturaId(factura.getId()).orElse(null)
                : null;
        boolean creditoVencido = credito != null && PENDIENTE.equals(credito.getEstado())
                && credito.getFechaLimite() != null
                && credito.getFechaLimite().isBefore(LocalDateTime.now());

        int minutosTolerancia = toleranciaSalidaService.obtenerMinutos(ingreso.getParqueaderoId());
        LocalDateTime fechaPago = factura != null && factura.getFechaPago() != null
                ? factura.getFechaPago()
                : (pago != null ? pago.getFecha() : null);
        boolean ingresoActivo = "ACTIVO".equalsIgnoreCase(ingreso.getEstado());
        LocalDateTime fechaLimiteSalida = ingresoActivo && pago != null
                ? toleranciaSalidaService.calcularFechaLimite(fechaPago, ingreso.getParqueaderoId())
                : null;
        boolean toleranciaVencida = fechaLimiteSalida != null
                && LocalDateTime.now().isAfter(fechaLimiteSalida);

        String estado;
        boolean alDia;
        if (creditoVencido) {
            estado = "DEUDA_VENCIDA";
            alDia = false;
        } else if (factura == null) {
            estado = pago != null ? "AL_DIA" : "SIN_FACTURA";
            alDia = true;
        } else if (PAGADA.equals(factura.getEstado()) && toleranciaVencida) {
            estado = "TOLERANCIA_VENCIDA";
            alDia = false;
        } else if (PAGADA.equals(factura.getEstado())) {
            estado = "AL_DIA";
            alDia = true;
        } else {
            estado = "PENDIENTE";
            alDia = false;
        }

        BigDecimal valorFactura = factura != null ? factura.getValor() : null;
        BigDecimal valorActual = valorFactura;
        if (factura != null) {
            try {
                valorActual = facturaService.calcularValorActual(ingreso);
            } catch (IllegalArgumentException ex) {
                valorActual = valorFactura;
            }
        }
        BigDecimal abonado = pago != null && pago.getValor() != null
                ? pago.getValor() : BigDecimal.ZERO;
        BigDecimal faltante = valorActual != null
                ? valorActual.subtract(abonado).max(BigDecimal.ZERO)
                : null;

        return EstadoPagoResponse.builder()
                .ingresoId(ingreso.getId())
                .folio(ingreso.getFolio())
                .placa(ingreso.getPlaca())
                .estadoIngreso(ingreso.getEstado())
                .alDia(alDia)
                .puedeSalir(alDia && !toleranciaVencida)
                .estado(estado)
                .facturaId(factura != null ? factura.getId() : null)
                .estadoFactura(factura != null ? factura.getEstado() : null)
                .valorFactura(valorFactura)
                .valorActual(valorActual)
                .faltante(faltante)
                .fechaPago(fechaPago)
                .minutosTolerancia(minutosTolerancia)
                .fechaLimiteSalida(fechaLimiteSalida)
                .pagoId(pago != null ? pago.getId() : null)
                .estadoPago(pago != null ? pago.getEstadoPago() : null)
                .creditoId(credito != null ? credito.getId() : null)
                .creditoPendiente(credito != null ? credito.getMontoPendiente() : null)
                .creditoVencido(creditoVencido)
                .build();
    }
}
