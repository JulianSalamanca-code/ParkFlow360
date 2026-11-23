package com.parkflow360.modulos.pagos.service;

import com.parkflow360.modulos.creditos.entity.Credito;
import com.parkflow360.modulos.creditos.repository.CreditoRepository;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.dto.FacturaResponse;
import com.parkflow360.modulos.pagos.entity.Factura;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.FacturaRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.saldos.service.SaldoService;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

/** Genera la factura de cada ingreso y gestiona su cobro con saldo. */
@Service
@RequiredArgsConstructor
public class FacturaService {

    private static final String PENDIENTE = "PENDIENTE";
    private static final String PAGADA = "PAGADA";
    private static final String CONFIRMADO = "CONFIRMADO";

    private final FacturaRepository facturaRepository;
    private final PagoRepository pagoRepository;
    private final IngresoRepository ingresoRepository;
    private final TarifaRepository tarifaRepository;
    private final CreditoRepository creditoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SaldoService saldoService;
    private final AccesoParqueaderoService accesoParqueaderoService;

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarTodas(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return facturaRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        Collection<Long> permitidos = accesoParqueaderoService.parqueaderosPermitidos(email);
        return facturaRepository.findAll().stream()
                .filter(factura -> {
                    Ingreso ingreso = ingresoRepository.findById(factura.getIngresoId()).orElse(null);
                    return ingreso != null && permitidos.contains(ingreso.getParqueaderoId());
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FacturaResponse> listarMias(String email) {
        Usuario usuario = accesoParqueaderoService.requireUsuario(email);
        return facturaRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FacturaResponse obtenerPorId(String email, Long id) {
        Factura factura = requireFactura(id);
        Ingreso ingreso = requireIngreso(factura.getIngresoId());
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());
        return mapToResponse(factura, ingreso);
    }

    /**
     * Genera como máximo una factura por ingreso e intenta cobrarla con saldo.
     * Se mantiene como fallback para ingresos anteriores que no abrieron factura al entrar.
     */
    @Transactional
    public FacturaResponse generarYCobrar(Ingreso ingreso) {
        return generarYCobrar(ingreso, null);
    }

    /**
     * Abre la factura con una unidad de tarifa: al entrar (RN-09/RN-11) o al expirar
     * por no-show (RN-10). La factura es única por ingreso, por lo que no se duplica.
     */
    @Transactional
    public FacturaResponse generarYCobrarUnaHora(Ingreso ingreso) {
        return generarYCobrar(ingreso, calcularValorHora(ingreso));
    }

    /**
     * Ajusta la factura abierta al entrar con el valor real de la estancia.
     * La diferencia se debita del saldo y se refleja en el mismo pago confirmado.
     */
    @Transactional
    public FacturaResponse ajustarAlSalir(Ingreso ingreso) {
        Optional<Factura> existente = facturaRepository.findByIngresoId(ingreso.getId());
        if (existente.isEmpty()) {
            return generarYCobrar(ingreso);
        }

        Factura factura = existente.get();
        BigDecimal valorFinal = calcularValor(ingreso);
        Pago pago = findPagoConfirmado(ingreso.getId()).orElse(null);
        BigDecimal abonado = pago != null && pago.getValor() != null
                ? pago.getValor() : BigDecimal.ZERO;
        BigDecimal faltante = valorFinal.subtract(abonado);
        if (faltante.compareTo(BigDecimal.ZERO) <= 0) {
            return mapToResponse(factura, ingreso);
        }

        factura.setValor(valorFinal);
        if (ingreso.getUsuarioId() == null
                || !saldoService.debitarSiAlcanza(ingreso.getUsuarioId(), faltante,
                        "FACTURA-" + factura.getId() + "-AJUSTE")) {
            factura.setEstado(PENDIENTE);
            facturaRepository.save(factura);
            return mapToResponse(factura, ingreso);
        }

        if (pago != null) {
            pago.setValor(abonado.add(faltante));
            pagoRepository.save(pago);
        } else {
            registrarPago(ingreso, factura, "SALDO");
        }
        marcarPagada(factura);
        return mapToResponse(factura, ingreso);
    }

    private FacturaResponse generarYCobrar(Ingreso ingreso, BigDecimal valorHora) {
        Optional<Factura> existente = facturaRepository.findByIngresoId(ingreso.getId());
        if (existente.isPresent()) {
            return mapToResponse(existente.get());
        }

        Factura factura = facturaRepository.save(Factura.builder()
                .ingresoId(ingreso.getId())
                .usuarioId(ingreso.getUsuarioId())
                .valor(valorHora != null ? valorHora : calcularValor(ingreso))
                .estado(PENDIENTE)
                .build());

        if (pagoRepository.existsByIngresoIdAndEstadoPago(ingreso.getId(), CONFIRMADO)) {
            marcarPagada(factura);
            return mapToResponse(factura);
        }

        if (ingreso.getUsuarioId() != null
                && saldoService.debitarSiAlcanza(ingreso.getUsuarioId(), factura.getValor(),
                        "FACTURA-" + factura.getId())) {
            registrarPago(ingreso, factura, "SALDO");
            marcarPagada(factura);
        }

        return mapToResponse(factura);
    }

    /**
     * Reintenta el cobro de una factura pendiente o abona una deuda de crédito.
     * Mantiene la ruta POST /api/facturas/{id}/pagar como único punto de entrada.
     */
    @Transactional
    public FacturaResponse pagar(String email, Long id) {
        Factura factura = requireFactura(id);
        Ingreso ingreso = requireIngreso(factura.getIngresoId());
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        if (PAGADA.equals(factura.getEstado())) {
            return abonarCredito(factura);
        }

        Pago pago = findPagoConfirmado(ingreso.getId()).orElse(null);
        BigDecimal abonado = pago != null && pago.getValor() != null
                ? pago.getValor() : BigDecimal.ZERO;
        BigDecimal faltante = factura.getValor().subtract(abonado);
        if (faltante.compareTo(BigDecimal.ZERO) <= 0) {
            marcarPagada(factura);
            return mapToResponse(factura, ingreso);
        }

        if (factura.getUsuarioId() == null) {
            throw new IllegalArgumentException("La factura no tiene un usuario asociado para el cobro.");
        }
        if (!saldoService.debitarSiAlcanza(factura.getUsuarioId(), faltante,
                "FACTURA-" + factura.getId() + "-AJUSTE")) {
            throw new IllegalArgumentException("Saldo insuficiente para pagar la factura.");
        }

        if (pago != null) {
            pago.setValor(abonado.add(faltante));
            pagoRepository.save(pago);
        } else {
            registrarPago(ingreso, factura, "SALDO");
        }
        marcarPagada(factura);
        return mapToResponse(factura, ingreso);
    }

    /** Marca como pagada la factura de un ingreso cuando el cajero registra un pago confirmado. */
    @Transactional
    public void marcarPagadaPorIngreso(Long ingresoId) {
        facturaRepository.findByIngresoId(ingresoId).ifPresent(factura -> {
            if (PENDIENTE.equals(factura.getEstado())) {
                marcarPagada(factura);
            }
        });
    }

    private FacturaResponse abonarCredito(Factura factura) {
        Credito credito = creditoRepository.findByFacturaId(factura.getId())
                .orElseThrow(() -> new IllegalArgumentException("La factura ya está pagada."));
        if (!"PENDIENTE".equals(credito.getEstado())) {
            throw new IllegalArgumentException("La factura ya está pagada y su crédito está saldado.");
        }

        BigDecimal abono = saldoService.saldoBloqueado(factura.getUsuarioId()).min(credito.getMontoPendiente());
        if (abono.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Saldo insuficiente para abonar el crédito.");
        }

        saldoService.debitarParaAbonoCredito(factura.getUsuarioId(), abono,
                "CREDITO-" + credito.getId());
        BigDecimal pendiente = credito.getMontoPendiente().subtract(abono);
        credito.setMontoPendiente(pendiente);
        if (pendiente.compareTo(BigDecimal.ZERO) == 0) {
            credito.setEstado("PAGADO");
        }
        creditoRepository.save(credito);
        return mapToResponse(factura);
    }

    /** El pago confirmado es único por ingreso (V18); se reutiliza para ajustar el valor final. */
    private Optional<Pago> findPagoConfirmado(Long ingresoId) {
        return pagoRepository.findByIngresoId(ingresoId).stream()
                .filter(pago -> CONFIRMADO.equals(pago.getEstadoPago()))
                .findFirst();
    }

    private void registrarPago(Ingreso ingreso, Factura factura, String metodoPago) {
        pagoRepository.save(Pago.builder()
                .ingresoId(ingreso.getId())
                .usuarioId(factura.getUsuarioId())
                .parqueaderoId(ingreso.getParqueaderoId())
                .vehiculoId(ingreso.getVehiculoId())
                .espacioId(ingreso.getEspacioId())
                .tarifaId(ingreso.getTarifaId())
                .valor(factura.getValor())
                .referencia("FACTURA-" + factura.getId())
                .metodoPago(metodoPago)
                .estado("PAGADO")
                .estadoPago(CONFIRMADO)
                .build());
    }

    private void marcarPagada(Factura factura) {
        factura.setEstado(PAGADA);
        factura.setFechaPago(LocalDateTime.now());
        facturaRepository.save(factura);
    }

    /** Una unidad de la tarifa: una hora si es tarifa HORA, el valor único en los demás tipos. */
    private BigDecimal calcularValorHora(Ingreso ingreso) {
        BigDecimal tarifaValor = ingreso.getTarifaValor();
        if (tarifaValor == null && ingreso.getTarifaId() != null) {
            tarifaValor = tarifaRepository.findById(ingreso.getTarifaId())
                    .map(Tarifa::getValor)
                    .orElse(null);
        }
        if (tarifaValor == null) {
            throw new IllegalArgumentException("No se pudo determinar el valor de la factura del ingreso.");
        }
        return tarifaValor;
    }

    /** RN-03: tarifas HORA cobran por horas desde la llegada; las demás, una sola vez. */
    private BigDecimal calcularValor(Ingreso ingreso) {
        BigDecimal tarifaValor = ingreso.getTarifaValor();
        boolean esHora = false;
        if (ingreso.getTarifaId() != null) {
            Optional<Tarifa> tarifa = tarifaRepository.findById(ingreso.getTarifaId());
            if (tarifa.isPresent()) {
                esHora = "HORA".equalsIgnoreCase(tarifa.get().getTipo());
                if (tarifaValor == null) {
                    tarifaValor = tarifa.get().getValor();
                }
            }
        }
        if (tarifaValor == null) {
            throw new IllegalArgumentException("No se pudo determinar el valor de la factura del ingreso.");
        }
        if (!esHora) {
            return tarifaValor;
        }

        LocalDateTime inicio = ingreso.getFechaLlegada();
        if (inicio == null) {
            inicio = ingreso.getFechaEntrada() != null ? ingreso.getFechaEntrada() : LocalDateTime.now();
        }
        LocalDateTime fin = ingreso.getFechaSalida() != null
                ? ingreso.getFechaSalida()
                : LocalDateTime.now();
        long minutos = Math.max(0, Duration.between(inicio, fin).toMinutes());
        long horas = Math.max(1, (long) Math.ceil(minutos / 60.0));
        return tarifaValor.multiply(BigDecimal.valueOf(horas));
    }

    private Factura requireFactura(Long id) {
        return facturaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Factura no encontrada con id: " + id));
    }

    private Ingreso requireIngreso(Long id) {
        return ingresoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + id));
    }

    private FacturaResponse mapToResponse(Factura factura) {
        return mapToResponse(factura, ingresoRepository.findById(factura.getIngresoId()).orElse(null));
    }

    private FacturaResponse mapToResponse(Factura factura, Ingreso ingreso) {
        Credito credito = creditoRepository.findByFacturaId(factura.getId()).orElse(null);
        boolean vencido = credito != null && "PENDIENTE".equals(credito.getEstado())
                && credito.getFechaLimite() != null
                && credito.getFechaLimite().isBefore(LocalDateTime.now());
        return FacturaResponse.builder()
                .id(factura.getId())
                .ingresoId(factura.getIngresoId())
                .usuarioId(factura.getUsuarioId())
                .parqueaderoId(ingreso != null ? ingreso.getParqueaderoId() : null)
                .valor(factura.getValor())
                .estado(factura.getEstado())
                .fechaEmision(factura.getFechaEmision())
                .fechaPago(factura.getFechaPago())
                .creditoId(credito != null ? credito.getId() : null)
                .creditoPendiente(credito != null ? credito.getMontoPendiente() : null)
                .creditoVencido(vencido)
                .build();
    }
}
