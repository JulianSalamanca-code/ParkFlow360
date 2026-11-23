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
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        String estado;
        boolean alDia;
        if (creditoVencido) {
            estado = "DEUDA_VENCIDA";
            alDia = false;
        } else if (factura == null) {
            estado = pago != null ? "AL_DIA" : "SIN_FACTURA";
            alDia = true;
        } else if (PAGADA.equals(factura.getEstado())) {
            estado = "AL_DIA";
            alDia = true;
        } else {
            estado = "PENDIENTE";
            alDia = false;
        }

        return EstadoPagoResponse.builder()
                .ingresoId(ingreso.getId())
                .folio(ingreso.getFolio())
                .placa(ingreso.getPlaca())
                .estadoIngreso(ingreso.getEstado())
                .alDia(alDia)
                .estado(estado)
                .facturaId(factura != null ? factura.getId() : null)
                .estadoFactura(factura != null ? factura.getEstado() : null)
                .valorFactura(factura != null ? factura.getValor() : null)
                .pagoId(pago != null ? pago.getId() : null)
                .estadoPago(pago != null ? pago.getEstadoPago() : null)
                .creditoId(credito != null ? credito.getId() : null)
                .creditoPendiente(credito != null ? credito.getMontoPendiente() : null)
                .creditoVencido(creditoVencido)
                .build();
    }
}
