package com.parkflow360.modulos.pagos.service;

import com.parkflow360.modulos.pagos.dto.PagoRequest;
import com.parkflow360.modulos.pagos.dto.PagoResponse;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<PagoResponse> listarTodos() {
        return pagoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorVehiculo(Long vehiculoId) {
        return pagoRepository.findByVehiculoId(vehiculoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorEspacio(Long espacioId) {
        return pagoRepository.findByEspacioId(espacioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return pagoRepository.findByFechaBetween(inicio, fin).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /** Pagos del cliente autenticado. */
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorUsuarioEmail(String email) {
        Usuario usuario = requireUsuario(email);
        return pagoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagoResponse obtenerPorId(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado con id: " + id));
        return mapToResponse(pago);
    }

    @Transactional
    public PagoResponse crear(PagoRequest request) {
        Pago pago = Pago.builder()
                .vehiculoId(request.getVehiculoId())
                .espacioId(request.getEspacioId())
                .tarifaId(request.getTarifaId())
                .valor(request.getValor())
                .metodoPago(request.getMetodoPago())
                .estado("PAGADO")
                .build();

        Pago guardado = pagoRepository.save(pago);
        return mapToResponse(guardado);
    }

    @Transactional
    public PagoResponse actualizar(Long id, PagoRequest request) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado con id: " + id));

        pago.setVehiculoId(request.getVehiculoId());
        pago.setEspacioId(request.getEspacioId());
        pago.setTarifaId(request.getTarifaId());
        pago.setValor(request.getValor());
        pago.setMetodoPago(request.getMetodoPago());

        Pago actualizado = pagoRepository.save(pago);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!pagoRepository.existsById(id)) {
            throw new NoSuchElementException("Pago no encontrado con id: " + id);
        }
        pagoRepository.deleteById(id);
    }

    private Usuario requireUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + email));
    }

    PagoResponse mapToResponse(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .vehiculoId(pago.getVehiculoId())
                .espacioId(pago.getEspacioId())
                .tarifaId(pago.getTarifaId())
                .valor(pago.getValor())
                .fecha(pago.getFecha())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .referencia(pago.getReferencia())
                .wompiTransactionId(pago.getWompiTransactionId())
                .moneda(pago.getMoneda())
                .usuarioId(pago.getUsuarioId())
                .ingresoId(pago.getIngresoId())
                .build();
    }
}
