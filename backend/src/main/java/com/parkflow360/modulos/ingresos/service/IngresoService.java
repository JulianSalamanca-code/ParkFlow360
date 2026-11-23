package com.parkflow360.modulos.ingresos.service;

import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.creditos.service.CreditoService;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.dto.IngresoRequest;
import com.parkflow360.modulos.ingresos.dto.IngresoResponse;
import com.parkflow360.modulos.ingresos.dto.VerificarIngresoRequest;
import com.parkflow360.modulos.ingresos.entity.Ingreso;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.pagos.service.FacturaService;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.modulos.vehiculos.entity.Vehiculo;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IngresoService {

    private static final Map<String, String> MODALIDAD_POR_TIPO = Map.of(
            "HORA", "POR HORAS",
            "DIA", "POR DÍA",
            "SEMANA", "POR SEMANA",
            "MES", "POR MES",
            "MOMENTO", "POR MOMENTO");

    private final IngresoRepository ingresoRepository;
    private final PagoRepository pagoRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TarifaRepository tarifaRepository;
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

        LocalDateTime fechaReserva = LocalDateTime.now();
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
                .modalidad(resolverModalidad(request.getTarifaId()))
                .operador(usuario.getNombre() != null ? usuario.getNombre() : usuario.getEmail())
                .novedad(request.getNovedad())
                .estado("RESERVADO")
                .usuarioId(usuario.getId())
                .fechaReserva(fechaReserva)
                .fechaLimiteLlegada(fechaReserva.plusHours(1))
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
                .modalidad(resolverModalidad(request.getTarifaId()))
                .operador(request.getOperador())
                .novedad(request.getNovedad())
                .estado("ACTIVO")
                .fechaLlegada(LocalDateTime.now())
                .build();

        ingreso = ingresoRepository.save(ingreso);
        ingreso.setFolio(String.format("TK-%06d", 8941 + ingreso.getId()));
        ingreso = ingresoRepository.save(ingreso);

        marcarEspacio(request.getEspacioId(), "OCUPADO");
        facturaService.generarYCobrarUnaHora(ingreso);

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
        facturaService.ajustarAlSalir(ingreso);

        return mapToResponse(ingreso);
    }

    /**
     * RN-09/RN-11: registra la llegada del guardia y abre la factura con una unidad
     * de tarifa. Tanto puntual como tardía, la hora de espera queda incluida en esa
     * única factura, que se ajustará al valor real al registrar la salida.
     */
    @Transactional
    public IngresoResponse registrarLlegada(String email, Long id) {
        Ingreso ingreso = ingresoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        if ("CANCELADO".equalsIgnoreCase(ingreso.getEstado())) {
            throw new IllegalArgumentException(
                    "La reserva ya fue cancelada por no-show; la hora de espera ya fue facturada. Realiza una nueva reserva.");
        }
        if (!"RESERVADO".equalsIgnoreCase(ingreso.getEstado())) {
            throw new IllegalArgumentException("Solo se puede registrar la llegada de una reserva vigente.");
        }

        ingreso.setFechaLlegada(LocalDateTime.now());
        ingreso.setEstado("ACTIVO");
        ingreso = ingresoRepository.save(ingreso);
        marcarEspacio(ingreso.getEspacioId(), "OCUPADO");
        facturaService.generarYCobrarUnaHora(ingreso);

        return mapToResponse(ingreso);
    }

    /**
     * RN-10/RN-11: cancela las reservas vencidas, libera el espacio y factura una hora
     * únicamente para tarifas por horas. El bloqueo pesimista evita dobles cobros.
     */
    @Transactional
    public int expirarReservasVencidas(LocalDateTime ahora) {
        List<Ingreso> vencidas = ingresoRepository.findReservasVencidasParaExpirar(ahora);
        for (Ingreso ingreso : vencidas) {
            ingreso.setEstado("CANCELADO");
            ingreso.setFechaSalida(ahora);
            ingresoRepository.save(ingreso);
            marcarEspacio(ingreso.getEspacioId(), "LIBRE");
            if (esTarifaHora(ingreso)) {
                facturaService.generarYCobrarUnaHora(ingreso);
            }
        }
        return vencidas.size();
    }

    /**
     * F7: el guardia (o supervisor) verifica los datos del vehículo y completa
     * únicamente los campos que estaban vacíos, sin sobrescribir información válida.
     */
    @Transactional
    public IngresoResponse verificar(String email, Long id, VerificarIngresoRequest request) {
        Ingreso ingreso = ingresoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException("Ingreso no encontrado con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, ingreso.getParqueaderoId());

        if (!"RESERVADO".equalsIgnoreCase(ingreso.getEstado())
                && !"ACTIVO".equalsIgnoreCase(ingreso.getEstado())) {
            throw new IllegalArgumentException("Solo se puede verificar un ingreso reservado o activo.");
        }

        if (request.getVehiculoId() != null) {
            Vehiculo vehiculo = vehiculoRepository.findById(request.getVehiculoId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Vehículo no encontrado con id: " + request.getVehiculoId()));
            validarVehiculo(ingreso, vehiculo);
            if (!tieneTexto(ingreso.getPlaca())) {
                ingreso.setPlaca(vehiculo.getPlaca());
            }
            if (ingreso.getVehiculoId() == null) {
                ingreso.setVehiculoId(vehiculo.getId());
            }
        }

        if (tieneTexto(request.getPlaca())) {
            String placa = request.getPlaca().trim().toUpperCase();
            if (tieneTexto(ingreso.getPlaca()) && !ingreso.getPlaca().equalsIgnoreCase(placa)) {
                throw new IllegalArgumentException("La placa no coincide con la registrada en el ingreso.");
            }
            Vehiculo vehiculo = vehiculoRepository.findByPlaca(placa).orElse(null);
            if (vehiculo != null) {
                validarVehiculo(ingreso, vehiculo);
                if (ingreso.getVehiculoId() == null) {
                    ingreso.setVehiculoId(vehiculo.getId());
                }
            }
            if (!tieneTexto(ingreso.getPlaca())) {
                ingreso.setPlaca(placa);
            }
        }

        completarSiVacio(ingreso.getCategoria(), request.getCategoria(), ingreso::setCategoria);
        completarSiVacio(ingreso.getEspacioNumero(), request.getEspacioNumero(), ingreso::setEspacioNumero);
        completarSiVacio(ingreso.getOperador(), request.getOperador(), ingreso::setOperador);
        completarSiVacio(ingreso.getNovedad(), request.getNovedad(), ingreso::setNovedad);

        return mapToResponse(ingresoRepository.save(ingreso));
    }

    private void validarVehiculo(Ingreso ingreso, Vehiculo vehiculo) {
        if (ingreso.getUsuarioId() != null && vehiculo.getUsuarioId() != null
                && !ingreso.getUsuarioId().equals(vehiculo.getUsuarioId())) {
            throw new IllegalArgumentException("El vehículo no pertenece al usuario del ingreso.");
        }
        if (ingreso.getVehiculoId() != null && !ingreso.getVehiculoId().equals(vehiculo.getId())) {
            throw new IllegalArgumentException("El vehículo no coincide con el registrado en el ingreso.");
        }
        if (tieneTexto(ingreso.getPlaca()) && !ingreso.getPlaca().equalsIgnoreCase(vehiculo.getPlaca())) {
            throw new IllegalArgumentException("La placa del vehículo no coincide con la registrada en el ingreso.");
        }
    }

    private void completarSiVacio(String actual, String nuevo, java.util.function.Consumer<String> setter) {
        if (!tieneTexto(actual) && tieneTexto(nuevo)) {
            setter.accept(nuevo.trim());
        }
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.isBlank();
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

    private boolean esTarifaHora(Ingreso ingreso) {
        return ingreso.getTarifaId() != null
                && tarifaRepository.findById(ingreso.getTarifaId())
                        .map(tarifa -> "HORA".equalsIgnoreCase(tarifa.getTipo()))
                        .orElse(false);
    }

    /** RN-12: la modalidad se deriva del tipo de tarifa, no de un valor fijo enviado por el cliente. */
    private String resolverModalidad(Long tarifaId) {
        if (tarifaId == null) {
            return "SIN TARIFA";
        }
        return tarifaRepository.findById(tarifaId)
                .map(Tarifa::getTipo)
                .map(tipo -> {
                    String clave = tipo == null ? "" : tipo.trim().toUpperCase();
                    return MODALIDAD_POR_TIPO.getOrDefault(clave, clave.isEmpty() ? "SIN TARIFA" : clave);
                })
                .orElse("SIN TARIFA");
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
                .fechaReserva(ingreso.getFechaReserva())
                .fechaLimiteLlegada(ingreso.getFechaLimiteLlegada())
                .fechaLlegada(ingreso.getFechaLlegada())
                .build();
    }
}
