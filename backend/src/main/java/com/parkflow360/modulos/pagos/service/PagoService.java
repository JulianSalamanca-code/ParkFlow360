package com.parkflow360.modulos.pagos.service;

import com.parkflow360.modulos.pagos.dto.PagoRequest;
import com.parkflow360.modulos.pagos.dto.PagoResponse;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final IngresoRepository ingresoRepository;

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
        Ingreso ingreso = requireIngresoForPayment(request.getIngresoId());
        validarReferencias(request, ingreso);
        if (pagoRepository.existsByIngresoId(ingreso.getId())) {
            throw new IllegalArgumentException("El ingreso ya tiene un pago registrado.");
        }

        Pago pago = Pago.builder()
                .vehiculoId(ingreso.getVehiculoId())
                .espacioId(ingreso.getEspacioId())
                .tarifaId(ingreso.getTarifaId())
                .ingresoId(ingreso.getId())
                .valor(request.getValor())
                .metodoPago(request.getMetodoPago())
                .estadoPago(normalizarEstadoPago(request.getEstadoPago()))
                .build();

        Pago guardado = pagoRepository.save(pago);
        return mapToResponse(guardado);
    }

    @Transactional
    public PagoResponse actualizar(Long id, PagoRequest request) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado con id: " + id));

        if (pago.getIngresoId() != null) {
            if (request.getIngresoId() != null && !request.getIngresoId().equals(pago.getIngresoId())) {
                throw new IllegalArgumentException("No se puede cambiar el ingreso asociado a un pago.");
            }
            Ingreso ingreso = ingresoRepository.findByIdForUpdate(pago.getIngresoId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Ingreso asociado al pago no encontrado: " + pago.getIngresoId()));
            validarReferencias(request, ingreso);
            if (request.getEstadoPago() != null
                    && !normalizarEstadoPago(request.getEstadoPago()).equals(pago.getEstadoPago())
                    && !"ACTIVO".equalsIgnoreCase(ingreso.getEstado())) {
                throw new IllegalArgumentException("No se puede cambiar el resultado del pago de una estancia inactiva.");
            }
        } else if (request.getIngresoId() != null) {
            Ingreso ingreso = requireIngresoForPayment(request.getIngresoId());
            validarReferencias(request, ingreso);
            if (pagoRepository.existsByIngresoId(ingreso.getId())) {
                throw new IllegalArgumentException("El ingreso ya tiene un pago registrado.");
            }
            pago.setVehiculoId(ingreso.getVehiculoId());
            pago.setEspacioId(ingreso.getEspacioId());
            pago.setTarifaId(ingreso.getTarifaId());
            pago.setIngresoId(ingreso.getId());
        } else {
            pago.setVehiculoId(request.getVehiculoId());
            pago.setEspacioId(request.getEspacioId());
            pago.setTarifaId(request.getTarifaId());
        }
        pago.setValor(request.getValor());
        pago.setMetodoPago(request.getMetodoPago());
        if (request.getEstadoPago() != null) {
            pago.setEstadoPago(normalizarEstadoPago(request.getEstadoPago()));
        }

        Pago actualizado = pagoRepository.save(pago);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado con id: " + id));
        if (pago.getIngresoId() != null) {
            Ingreso ingreso = ingresoRepository.findByIdForUpdate(pago.getIngresoId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Ingreso asociado al pago no encontrado: " + pago.getIngresoId()));
            if ("FINALIZADO".equalsIgnoreCase(ingreso.getEstado())) {
                throw new IllegalArgumentException("No se puede eliminar el pago de una estancia finalizada.");
            }
        }
        pagoRepository.delete(pago);
    }

    private Ingreso requireIngresoForPayment(Long ingresoId) {
        if (ingresoId == null) {
            throw new IllegalArgumentException("Debes asociar el pago a un ingreso activo.");
        }

        Ingreso ingreso = ingresoRepository.findByIdForUpdate(ingresoId)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + ingresoId));
        if (!"ACTIVO".equalsIgnoreCase(ingreso.getEstado())) {
            throw new IllegalArgumentException("Solo se puede registrar el pago de un ingreso activo.");
        }
        return ingreso;
    }

    private void validarReferencias(PagoRequest request, Ingreso ingreso) {
        if (request.getVehiculoId() != null && !Objects.equals(request.getVehiculoId(), ingreso.getVehiculoId())) {
            throw new IllegalArgumentException("El vehículo del pago no coincide con el ingreso.");
        }
        if (request.getEspacioId() != null && !Objects.equals(request.getEspacioId(), ingreso.getEspacioId())) {
            throw new IllegalArgumentException("El espacio del pago no coincide con el ingreso.");
        }
        if (request.getTarifaId() != null && !Objects.equals(request.getTarifaId(), ingreso.getTarifaId())) {
            throw new IllegalArgumentException("La tarifa del pago no coincide con el ingreso.");
        }
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
                .ingresoId(pago.getIngresoId())
                .valor(pago.getValor())
                .fecha(pago.getFecha())
                .metodoPago(pago.getMetodoPago())
                .build();
    }

    private String normalizarEstadoPago(String estadoPago) {
        if (estadoPago == null || estadoPago.isBlank()) {
            return "CONFIRMADO";
        }
        String estadoNormalizado = estadoPago.toUpperCase(Locale.ROOT);
        if (!"CONFIRMADO".equals(estadoNormalizado) && !"FALLIDO".equals(estadoNormalizado)) {
            throw new IllegalArgumentException("El estado del pago debe ser CONFIRMADO o FALLIDO.");
        }
        return estadoNormalizado;
    }
}
