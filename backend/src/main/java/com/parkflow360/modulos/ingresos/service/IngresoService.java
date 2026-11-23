package com.parkflow360.modulos.ingresos.service;

import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.creditos.service.CreditoService;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.pagos.service.FacturaService;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngresoService {

    private final IngresoRepository ingresoRepository;
    private final PagoRepository pagoRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final AccesoParqueaderoService accesoParqueaderoService;
    private final FacturaService facturaService;
    private final CreditoService creditoService;

    @Transactional(readOnly = true)
    public List<IngresoResponse> listarTodos(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return ingresoRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return filtrarPorParqueaderos(accesoParqueaderoService.parqueaderosPermitidos(email),
                ingresoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<IngresoResponse> listarActivos(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return ingresoRepository.findByEstado("ACTIVO").stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        return filtrarPorParqueaderos(accesoParqueaderoService.parqueaderosPermitidos(email),
                ingresoRepository.findByEstado("ACTIVO"));
    }

    @Transactional(readOnly = true)
    public IngresoResponse obtenerPorId(String email, Long id) {
        Ingreso ingreso = requireIngreso(id);
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());
        return mapToResponse(ingreso);
    }

    @Transactional(readOnly = true)
    public IngresoResponse obtenerPorFolio(String email, String folio) {
        Ingreso ingreso = ingresoRepository.findByFolio(folio)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con folio: " + folio));
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());
        return mapToResponse(ingreso);
    }

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
        creditoService.validarSinDeudaVencida(usuario.getId());
        Long parqueaderoId = resolverParqueadero(email, request);

        String placa = request.getPlaca() != null ? request.getPlaca().toUpperCase() : null;

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
                .parqueaderoId(parqueaderoId)
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

    @Transactional
    public IngresoResponse crear(String email, IngresoRequest request) {
        Long parqueaderoId = resolverParqueadero(email, request);

        Ingreso ingreso = Ingreso.builder()
                .parqueaderoId(parqueaderoId)
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
    public IngresoResponse registrarSalida(String email, Long id) {
        Ingreso ingreso = ingresoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        if (!"ACTIVO".equalsIgnoreCase(ingreso.getEstado())) {
            throw new IllegalArgumentException("Solo se puede registrar la salida de un ingreso activo.");
        }

        ingreso.setEstado("FINALIZADO");
        ingreso.setFechaSalida(LocalDateTime.now());
        ingreso = ingresoRepository.save(ingreso);

        marcarEspacio(ingreso.getEspacioId(), "LIBRE");
        facturaService.generarYCobrar(ingreso);

        return mapToResponse(ingreso);
    }

    @Transactional
    public IngresoResponse liberar(String email, Long id) {
        Ingreso ingreso = requireIngreso(id);
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        if ("FINALIZADO".equalsIgnoreCase(ingreso.getEstado()) || "CANCELADO".equalsIgnoreCase(ingreso.getEstado())) {
            throw new IllegalArgumentException("Esta reserva ya fue finalizada o liberada.");
        }
        if (pagoRepository.existsByIngresoIdAndEstadoPago(id, "CONFIRMADO")) {
            throw new IllegalArgumentException("No se puede liberar una estancia con un pago confirmado; registra la salida.");
        }

        ingreso.setEstado("CANCELADO");
        ingreso.setFechaSalida(LocalDateTime.now());
        ingreso = ingresoRepository.save(ingreso);

        marcarEspacio(ingreso.getEspacioId(), "LIBRE");

        return mapToResponse(ingreso);
    }

    @Transactional
    public IngresoResponse liberarPorEspacio(String email, Long espacioId) {
        Espacio espacio = espacioRepository.findById(espacioId)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado con id: " + espacioId));
        accesoParqueaderoService.verificarAcceso(email, espacio.getParqueaderoId());

        Ingreso ingreso = ingresoRepository
                .findFirstByEspacioIdAndEstadoIn(espacioId, List.of("RESERVADO", "ACTIVO"))
                .orElseThrow(() -> new IllegalArgumentException(
                    "El espacio no tiene una reserva o parqueo activo que liberar."));
        return liberar(email, ingreso.getId());
    }

    @Transactional
    public void eliminar(String email, Long id) {
        Ingreso ingreso = requireIngreso(id);
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        if (pagoRepository.existsByIngresoId(id)) {
            throw new IllegalArgumentException("No se puede eliminar una estancia que tiene un pago asociado.");
        }
        ingresoRepository.deleteById(id);
    }

    private Long resolverParqueadero(String email, IngresoRequest request) {
        Long solicitado = request.getParqueaderoId();
        if (solicitado == null && request.getEspacioId() != null) {
            Espacio espacio = espacioRepository.findById(request.getEspacioId()).orElse(null);
            if (espacio != null) {
                solicitado = espacio.getParqueaderoId();
            }
        }
        return accesoParqueaderoService.resolverParqueadero(email, solicitado);
    }

    private List<IngresoResponse> filtrarPorParqueaderos(Collection<Long> permitidos, List<Ingreso> ingresos) {
        return ingresos.stream()
                .filter(ingreso -> ingreso.getParqueaderoId() == null
                        || permitidos.contains(ingreso.getParqueaderoId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Ingreso requireIngreso(Long id) {
        return ingresoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + id));
    }

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
        var pago = pagoRepository.findByIngresoId(ingreso.getId()).stream()
                .filter(p -> "CONFIRMADO".equals(p.getEstadoPago()))
                .findFirst()
                .orElseGet(() -> pagoRepository.findByIngresoId(ingreso.getId()).stream()
                        .findFirst()
                        .orElse(null));
        return IngresoResponse.builder()
                .id(ingreso.getId())
                .parqueaderoId(ingreso.getParqueaderoId())
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
                .pagoId(pago != null ? pago.getId() : null)
                .pagoRegistrado(pago != null && "CONFIRMADO".equals(pago.getEstadoPago()))
                .estadoPago(pago != null ? pago.getEstadoPago() : null)
                .fechaEntrada(ingreso.getFechaEntrada())
                .fechaSalida(ingreso.getFechaSalida())
                .build();
    }
}
