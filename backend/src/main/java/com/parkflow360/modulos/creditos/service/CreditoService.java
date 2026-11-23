package com.parkflow360.modulos.creditos.service;

import com.parkflow360.modulos.creditos.dto.CreditoRequest;
import com.parkflow360.modulos.creditos.dto.CreditoResponse;
import com.parkflow360.modulos.creditos.entity.Credito;
import com.parkflow360.modulos.creditos.repository.CreditoRepository;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.entity.Factura;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.FacturaRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.saldos.service.SaldoService;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** Crédito del faltante de una factura, sin intereses y con vencimiento a 3 días. */
@Service
@RequiredArgsConstructor
public class CreditoService {

    private static final int DIAS_VENCIMIENTO = 3;
    private static final String PENDIENTE = "PENDIENTE";
    private static final String PAGADA = "PAGADA";
    private static final String CONFIRMADO = "CONFIRMADO";

    private final CreditoRepository creditoRepository;
    private final FacturaRepository facturaRepository;
    private final IngresoRepository ingresoRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SaldoService saldoService;
    private final AccesoParqueaderoService accesoParqueaderoService;

    @Transactional(readOnly = true)
    public List<CreditoResponse> listarMios(String email) {
        Usuario usuario = accesoParqueaderoService.requireUsuario(email);
        return creditoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CreditoResponse> listarTodos(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return creditoRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        Collection<Long> permitidos = accesoParqueaderoService.parqueaderosPermitidos(email);
        return creditoRepository.findAll().stream()
                .filter(credito -> {
                    Factura factura = facturaRepository.findById(credito.getFacturaId()).orElse(null);
                    if (factura == null) {
                        return false;
                    }
                    Ingreso ingreso = ingresoRepository.findById(factura.getIngresoId()).orElse(null);
                    return ingreso != null && permitidos.contains(ingreso.getParqueaderoId());
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cubre solo el faltante de una factura pendiente. El saldo disponible se descuenta
     * primero y la factura pasa a PAGADA; la deuda queda registrada en creditos.
     */
    @Transactional
    public CreditoResponse otorgar(String email, CreditoRequest request) {
        Usuario usuario = accesoParqueaderoService.requireUsuario(email);
        Factura factura = facturaRepository.findById(request.getFacturaId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Factura no encontrada con id: " + request.getFacturaId()));
        if (!usuario.getId().equals(factura.getUsuarioId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "La factura no pertenece al usuario autenticado.");
        }
        Ingreso ingreso = ingresoRepository.findById(factura.getIngresoId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Ingreso no encontrado con id: " + factura.getIngresoId()));
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        if (!PENDIENTE.equals(factura.getEstado())) {
            throw new IllegalArgumentException("Solo se puede solicitar crédito para una factura pendiente.");
        }
        if (creditoRepository.existsByUsuarioIdAndEstado(usuario.getId(), PENDIENTE)) {
            throw new IllegalArgumentException("Ya tienes un crédito pendiente; debes abonarlo antes de solicitar otro.");
        }

        BigDecimal saldo = saldoService.saldoBloqueado(usuario.getId());
        if (saldo.compareTo(factura.getValor()) >= 0) {
            throw new IllegalArgumentException(
                    "Tu saldo alcanza para pagar la factura; usa POST /api/facturas/{id}/pagar.");
        }
        BigDecimal debitado = BigDecimal.ZERO;
        if (saldo.compareTo(BigDecimal.ZERO) > 0) {
            debitado = saldoService.debitarHasta(usuario.getId(), factura.getValor(),
                    "FACTURA-" + factura.getId());
        }
        BigDecimal monto = factura.getValor().subtract(debitado);
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("No hay un faltante que cubrir con crédito.");
        }

        Credito credito = creditoRepository.save(Credito.builder()
                .usuarioId(usuario.getId())
                .facturaId(factura.getId())
                .monto(monto)
                .montoPendiente(monto)
                .fechaLimite(LocalDateTime.now().plusDays(DIAS_VENCIMIENTO))
                .estado(PENDIENTE)
                .build());

        registrarPago(ingreso, factura, debitado);
        factura.setEstado(PAGADA);
        factura.setFechaPago(LocalDateTime.now());
        facturaRepository.save(factura);

        return mapToResponse(credito);
    }

    /** Deuda vencida bloquea únicamente nuevas reservas. */
    @Transactional
    public void validarSinDeudaVencida(Long usuarioId) {
        LocalDateTime ahora = LocalDateTime.now();
        List<Credito> vencidos = creditoRepository
                .findByUsuarioIdAndEstadoAndFechaLimiteBefore(usuarioId, PENDIENTE, ahora);
        if (vencidos.isEmpty()) {
            return;
        }
        Credito masAntiguo = vencidos.stream()
                .min((a, b) -> a.getFechaLimite().compareTo(b.getFechaLimite()))
                .orElse(vencidos.get(0));
        vencidos.forEach(credito -> {
            credito.setEstado("VENCIDO");
            creditoRepository.save(credito);
        });
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Tienes una deuda vencida desde el " + masAntiguo.getFechaLimite()
                        + ". Abona tu crédito antes de reservar.");
    }

    private void registrarPago(Ingreso ingreso, Factura factura, BigDecimal debitado) {
        if (pagoRepository.existsByIngresoIdAndEstadoPago(ingreso.getId(), CONFIRMADO)) {
            return;
        }
        pagoRepository.save(Pago.builder()
                .ingresoId(ingreso.getId())
                .usuarioId(factura.getUsuarioId())
                .parqueaderoId(ingreso.getParqueaderoId())
                .vehiculoId(ingreso.getVehiculoId())
                .espacioId(ingreso.getEspacioId())
                .tarifaId(ingreso.getTarifaId())
                .valor(factura.getValor())
                .referencia("FACTURA-" + factura.getId())
                .metodoPago(debitado.compareTo(BigDecimal.ZERO) > 0 ? "SALDO_CREDITO" : "CREDITO")
                .estado("PAGADO")
                .estadoPago(CONFIRMADO)
                .build());
    }

    private CreditoResponse mapToResponse(Credito credito) {
        boolean vencido = PENDIENTE.equals(credito.getEstado())
                && credito.getFechaLimite() != null
                && credito.getFechaLimite().isBefore(LocalDateTime.now());
        return CreditoResponse.builder()
                .id(credito.getId())
                .usuarioId(credito.getUsuarioId())
                .facturaId(credito.getFacturaId())
                .monto(credito.getMonto())
                .montoPendiente(credito.getMontoPendiente())
                .fechaOtorgamiento(credito.getFechaOtorgamiento())
                .fechaLimite(credito.getFechaLimite())
                .estado(credito.getEstado())
                .vencido(vencido)
                .build();
    }
}
