package com.parkflow360.modulos.ingresos.service;

import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
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
public class IngresoService {

    private final IngresoRepository ingresoRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;

    // ---------- Consultas ----------

    @Transactional(readOnly = true)
    public List<IngresoResponse> listarTodos() {
        return ingresoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<IngresoResponse> listarActivos() {
        return ingresoRepository.findByEstado("ACTIVO").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IngresoResponse obtenerPorId(Long id) {
        Ingreso ingreso = ingresoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + id));
        return mapToResponse(ingreso);
    }

    @Transactional(readOnly = true)
    public IngresoResponse obtenerPorFolio(String folio) {
        Ingreso ingreso = ingresoRepository.findByFolio(folio)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con folio: " + folio));
        return mapToResponse(ingreso);
    }

    // ---------- Usuario (cliente) ----------

    @Transactional(readOnly = true)
    public List<IngresoResponse> listarPorUsuarioEmail(String email) {
        Usuario usuario = requireUsuario(email);
        return ingresoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public IngresoResponse reservar(String email, IngresoRequest request) {
        Usuario usuario = requireUsuario(email);

        String placa = request.getPlaca() != null ? request.getPlaca().toUpperCase() : null;

        // Regla: un vehículo solo puede tener una reserva/parqueo activo a la vez
        boolean yaTieneActivo = ingresoRepository
                .findByUsuarioIdAndEstadoIn(usuario.getId(), List.of("RESERVADO", "ACTIVO"))
                .stream()
                .anyMatch(i ->
                    (placa != null && placa.equalsIgnoreCase(i.getPlaca()))
                    || (request.getVehiculoId() != null && request.getVehiculoId().equals(i.getVehiculoId()))
                );

        if (yaTieneActivo) {
            throw new IllegalArgumentException(
                "Este vehículo ya tiene una reserva o parqueo activo. Debes finalizarlo antes de reservar de nuevo.");
        }

        Ingreso ingreso = Ingreso.builder()
                .folio("TMP-" + System.nanoTime())
                .placa(placa)
                .categoria(request.getCategoria())
                .vehiculoId(request.getVehiculoId())
                .espacioId(request.getEspacioId())
                .espacioNumero(request.getEspacioNumero())
                .tarifaId(request.getTarifaId())
                .tarifaValor(request.getTarifaValor())
                .modalidad(request.getModalidad())
                .operador(usuario.getNombre() != null ? usuario.getNombre() : usuario.getEmail())
                .novedad(request.getNovedad())
                .estado("RESERVADO")
                .usuarioId(usuario.getId())
                .build();

        ingreso = ingresoRepository.save(ingreso);
        ingreso.setFolio(String.format("RSV-%06d", 4001 + ingreso.getId()));
        ingreso = ingresoRepository.save(ingreso);

        marcarEspacio(request.getEspacioId(), "RESERVADO");

        return mapToResponse(ingreso);
    }

    // ---------- Administrador ----------

    @Transactional
    public IngresoResponse crear(IngresoRequest request) {
        Ingreso ingreso = Ingreso.builder()
                .folio("TMP-" + System.nanoTime())
                .placa(request.getPlaca() != null ? request.getPlaca().toUpperCase() : null)
                .categoria(request.getCategoria())
                .vehiculoId(request.getVehiculoId())
                .espacioId(request.getEspacioId())
                .espacioNumero(request.getEspacioNumero())
                .tarifaId(request.getTarifaId())
                .tarifaValor(request.getTarifaValor())
                .modalidad(request.getModalidad())
                .operador(request.getOperador())
                .novedad(request.getNovedad())
                .estado("ACTIVO")
                .build();

        ingreso = ingresoRepository.save(ingreso);
        ingreso.setFolio(String.format("TK-%06d", 8941 + ingreso.getId()));
        ingreso = ingresoRepository.save(ingreso);

        marcarEspacio(request.getEspacioId(), "OCUPADO");

        return mapToResponse(ingreso);
    }

    @Transactional
    public IngresoResponse registrarSalida(Long id) {
        Ingreso ingreso = ingresoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + id));

        ingreso.setEstado("FINALIZADO");
        ingreso.setFechaSalida(LocalDateTime.now());
        ingreso = ingresoRepository.save(ingreso);

        marcarEspacio(ingreso.getEspacioId(), "LIBRE");

        return mapToResponse(ingreso);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!ingresoRepository.existsById(id)) {
            throw new NoSuchElementException("Ingreso no encontrado con id: " + id);
        }
        ingresoRepository.deleteById(id);
    }

    // ---------- Helpers ----------

    private Usuario requireUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + email));
    }

    private void marcarEspacio(Long espacioId, String estado) {
        if (espacioId == null) {
            return;
        }
        espacioRepository.findById(espacioId).ifPresent(espacio -> {
            espacio.setEstado(estado);
            espacioRepository.save(espacio);
        });
    }

    private IngresoResponse mapToResponse(Ingreso ingreso) {
        return IngresoResponse.builder()
                .id(ingreso.getId())
                .folio(ingreso.getFolio())
                .placa(ingreso.getPlaca())
                .categoria(ingreso.getCategoria())
                .vehiculoId(ingreso.getVehiculoId())
                .espacioId(ingreso.getEspacioId())
                .espacioNumero(ingreso.getEspacioNumero())
                .tarifaId(ingreso.getTarifaId())
                .tarifaValor(ingreso.getTarifaValor())
                .modalidad(ingreso.getModalidad())
                .operador(ingreso.getOperador())
                .novedad(ingreso.getNovedad())
                .estado(ingreso.getEstado())
                .usuarioId(ingreso.getUsuarioId())
                .fechaEntrada(ingreso.getFechaEntrada())
                .fechaSalida(ingreso.getFechaSalida())
                .build();
    }
}
