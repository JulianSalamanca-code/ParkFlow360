package com.parkflow360.modulos.saldos.service;

import com.parkflow360.modulos.saldos.dto.MovimientoSaldoResponse;
import com.parkflow360.modulos.saldos.dto.RecargaRequest;
import com.parkflow360.modulos.saldos.dto.SaldoResponse;
import com.parkflow360.modulos.saldos.entity.MovimientoSaldo;
import com.parkflow360.modulos.saldos.repository.MovimientoSaldoRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/** Gestiona el saldo recargable y su ledger inmutable de movimientos. */
@Service
@RequiredArgsConstructor
public class SaldoService {

    private final UsuarioRepository usuarioRepository;
    private final MovimientoSaldoRepository movimientoSaldoRepository;

    @Transactional(readOnly = true)
    public SaldoResponse obtenerMiCuenta(String email) {
        Usuario usuario = requireUsuario(email);
        return construirResponse(usuario);
    }

    @Transactional
    public SaldoResponse recargar(String email, RecargaRequest request) {
        if (request.getValor() == null || request.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor de la recarga debe ser mayor a cero.");
        }

        Usuario usuario = requireUsuarioBloqueado(requireUsuario(email).getId());
        BigDecimal saldoAnterior = saldo(usuario);
        BigDecimal saldoPosterior = saldoAnterior.add(request.getValor());
        usuario.setSaldo(saldoPosterior);
        usuarioRepository.save(usuario);

        String referencia = request.getReferencia() != null && !request.getReferencia().isBlank()
                ? request.getReferencia()
                : "RECARGA-" + System.currentTimeMillis();
        registrarMovimiento(usuario.getId(), "RECARGA", request.getValor(),
                saldoAnterior, saldoPosterior, referencia);

        return construirResponse(usuario);
    }

    /** Consulta el saldo manteniendo el bloqueo de la fila durante la transacción. */
    @Transactional
    public BigDecimal saldoBloqueado(Long usuarioId) {
        return saldo(requireUsuarioBloqueado(usuarioId));
    }

    /** Debita el total solo si hay saldo suficiente; devuelve false sin crear movimientos. */
    @Transactional
    public boolean debitarSiAlcanza(Long usuarioId, BigDecimal valor, String referencia) {
        validarValor(valor);
        Usuario usuario = requireUsuarioBloqueado(usuarioId);
        if (saldo(usuario).compareTo(valor) < 0) {
            return false;
        }
        aplicarDebito(usuario, valor, referencia, "DEBITO");
        return true;
    }

    /** Debita como máximo {@code maximo} y devuelve el valor efectivamente debitado. */
    @Transactional
    public BigDecimal debitarHasta(Long usuarioId, BigDecimal maximo, String referencia) {
        validarValor(maximo);
        Usuario usuario = requireUsuarioBloqueado(usuarioId);
        BigDecimal debitado = saldo(usuario).min(maximo);
        if (debitado.compareTo(BigDecimal.ZERO) > 0) {
            aplicarDebito(usuario, debitado, referencia, "DEBITO");
        }
        return debitado;
    }

    /** Debita el saldo usado para abonar una deuda de crédito. */
    @Transactional
    public void debitarParaAbonoCredito(Long usuarioId, BigDecimal valor, String referencia) {
        validarValor(valor);
        Usuario usuario = requireUsuarioBloqueado(usuarioId);
        if (saldo(usuario).compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para abonar el crédito.");
        }
        aplicarDebito(usuario, valor, referencia, "PAGO_CREDITO");
    }

    private void aplicarDebito(Usuario usuario, BigDecimal valor, String referencia, String tipo) {
        BigDecimal saldoAnterior = saldo(usuario);
        BigDecimal saldoPosterior = saldoAnterior.subtract(valor);
        usuario.setSaldo(saldoPosterior);
        usuarioRepository.save(usuario);
        registrarMovimiento(usuario.getId(), tipo, valor, saldoAnterior, saldoPosterior, referencia);
    }

    private void registrarMovimiento(Long usuarioId, String tipo, BigDecimal valor,
                                     BigDecimal saldoAnterior, BigDecimal saldoPosterior,
                                     String referencia) {
        MovimientoSaldo movimiento = MovimientoSaldo.builder()
                .usuarioId(usuarioId)
                .tipo(tipo)
                .valor(valor)
                .saldoAnterior(saldoAnterior)
                .saldoPosterior(saldoPosterior)
                .referencia(referencia)
                .build();
        movimientoSaldoRepository.save(movimiento);
    }

    private SaldoResponse construirResponse(Usuario usuario) {
        List<MovimientoSaldoResponse> movimientos =
                movimientoSaldoRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId()).stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());
        return SaldoResponse.builder()
                .saldo(saldo(usuario))
                .movimientos(movimientos)
                .build();
    }

    private MovimientoSaldoResponse mapToResponse(MovimientoSaldo movimiento) {
        return MovimientoSaldoResponse.builder()
                .id(movimiento.getId())
                .tipo(movimiento.getTipo())
                .valor(movimiento.getValor())
                .saldoAnterior(movimiento.getSaldoAnterior())
                .saldoPosterior(movimiento.getSaldoPosterior())
                .referencia(movimiento.getReferencia())
                .fecha(movimiento.getFecha())
                .build();
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El valor debe ser mayor a cero.");
        }
    }

    private BigDecimal saldo(Usuario usuario) {
        return usuario.getSaldo() != null ? usuario.getSaldo() : BigDecimal.ZERO;
    }

    private Usuario requireUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + email));
    }

    private Usuario requireUsuarioBloqueado(Long usuarioId) {
        return usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + usuarioId));
    }
}
