package com.parkflow360.modulos.parqueaderos.service;

import com.parkflow360.modulos.empresas.entity.Empresa;
import com.parkflow360.modulos.empresas.repository.EmpresaRepository;
import com.parkflow360.modulos.empresas.repository.EmpresaUsuarioRepository;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.parqueaderos.dto.GuardiaRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoRequest;
import com.parkflow360.modulos.parqueaderos.dto.ParqueaderoResponse;
import com.parkflow360.modulos.parqueaderos.dto.TipoVehiculoRequest;
import com.parkflow360.modulos.parqueaderos.dto.TipoVehiculoResponse;
import com.parkflow360.modulos.parqueaderos.dto.VinculacionRequest;
import com.parkflow360.modulos.parqueaderos.entity.Parqueadero;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoTipoVehiculo;
import com.parkflow360.modulos.parqueaderos.entity.ParqueaderoUsuario;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoTipoVehiculoRepository;
import com.parkflow360.modulos.parqueaderos.repository.ParqueaderoUsuarioRepository;
import com.parkflow360.security.Roles;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParqueaderoService {

    private final ParqueaderoRepository parqueaderoRepository;
    private final ParqueaderoTipoVehiculoRepository tipoVehiculoRepository;
    private final ParqueaderoUsuarioRepository parqueaderoUsuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final EmpresaUsuarioRepository empresaUsuarioRepository;
    private final EspacioRepository espacioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccesoParqueaderoService accesoParqueaderoService;

    @Transactional
    public ParqueaderoResponse crear(Long empresaId, ParqueaderoRequest request, String emailActor) {
        verificarAccesoEmpresa(emailActor, empresaId);
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new NoSuchElementException("Empresa no encontrada con id: " + empresaId));

        String identificador = request.getIdentificador().trim().toUpperCase(Locale.ROOT);
        if (parqueaderoRepository.existsByIdentificador(identificador)) {
            throw new IllegalArgumentException(
                    "Ya existe un parqueadero con el identificador: " + identificador);
        }
        validarTipos(request.getTipos());

        Parqueadero parqueadero = parqueaderoRepository.save(Parqueadero.builder()
                .empresaId(empresa.getId())
                .nombre(request.getNombre().trim())
                .identificador(identificador)
                .capacidadVehiculos(request.getCapacidadVehiculos())
                .activo(true)
                .build());

        List<ParqueaderoTipoVehiculo> tipos = guardarTipos(parqueadero.getId(), request.getTipos());
        Usuario cajero = crearCajero(parqueadero, request);

        return mapToResponse(parqueadero, tipos, cajero.getEmail());
    }

    @Transactional
    public ParqueaderoResponse vincular(String email, VinculacionRequest request) {
        Usuario usuario = accesoParqueaderoService.requireUsuario(email);
        if (!Roles.USUARIO.equals(Roles.normalizar(usuario.getRol()))) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Solo un usuario cliente puede vincularse a parqueaderos.");
        }

        String identificador = request.getIdentificador().trim().toUpperCase(Locale.ROOT);
        Parqueadero parqueadero = parqueaderoRepository.findByIdentificador(identificador)
                .orElseThrow(() -> new NoSuchElementException(
                        "No existe un parqueadero con el identificador: " + identificador));

        if (!parqueaderoUsuarioRepository.existsByParqueaderoIdAndUsuarioIdAndRolAsignado(
                parqueadero.getId(), usuario.getId(), Roles.USUARIO)) {
            parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                    .parqueaderoId(parqueadero.getId())
                    .usuarioId(usuario.getId())
                    .rolAsignado(Roles.USUARIO)
                    .build());
        }

        return mapToResponse(parqueadero, null, null);
    }

    @Transactional(readOnly = true)
    public List<ParqueaderoResponse> listarMios(String email) {
        List<Long> ids = accesoParqueaderoService.parqueaderosPermitidos(email);
        if (ids.isEmpty()) {
            return List.of();
        }
        return parqueaderoRepository.findAllById(ids).stream()
                .map(parqueadero -> mapToResponse(parqueadero, null, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public void asignarGuardia(Long parqueaderoId, GuardiaRequest request, String emailActor) {
        accesoParqueaderoService.verificarAcceso(emailActor, parqueaderoId);
        if (!parqueaderoRepository.existsById(parqueaderoId)) {
            throw new NoSuchElementException("Parqueadero no encontrado con id: " + parqueaderoId);
        }

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        Usuario guardia = usuarioRepository.findByEmail(email).orElse(null);

        if (guardia == null) {
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new IllegalArgumentException(
                        "La contraseña es obligatoria cuando el guardia aún no tiene cuenta.");
            }
            guardia = usuarioRepository.save(Usuario.builder()
                    .email(email)
                    .password(passwordEncoder.encode(request.getPassword()))
                    .nombre(request.getNombre())
                    .rol(Roles.GUARDIA)
                    .build());
        } else {
            String rolActual = Roles.normalizar(guardia.getRol());
            if (Roles.CAJERO_PAGOS.equals(rolActual)) {
                throw new IllegalArgumentException("El usuario indicado es cajero de un parqueadero.");
            }
            if (parqueaderoUsuarioRepository.existsByUsuarioIdAndRolAsignado(
                    guardia.getId(), Roles.GUARDIA)) {
                throw new IllegalArgumentException("El guardia ya pertenece a un parqueadero.");
            }
            guardia.setRol(Roles.GUARDIA);
            if (request.getNombre() != null && !request.getNombre().isBlank()) {
                guardia.setNombre(request.getNombre());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                guardia.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            usuarioRepository.save(guardia);
        }

        parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                .parqueaderoId(parqueaderoId)
                .usuarioId(guardia.getId())
                .rolAsignado(Roles.GUARDIA)
                .build());
    }

    /** RN-14: impide crear un espacio por encima de un cupo fijo configurado. */
    @Transactional(readOnly = true)
    public void validarCupo(Long parqueaderoId, String tipo) {
        validarCupo(parqueaderoId, tipo, 1);
    }

    /** Valida el cupo considerando también los espacios creados en el mismo lote. */
    @Transactional(readOnly = true)
    public void validarCupo(Long parqueaderoId, String tipo, int solicitadosEnLote) {
        if (parqueaderoId == null || tipo == null) {
            return;
        }
        int nuevos = Math.max(1, solicitadosEnLote);
        tipoVehiculoRepository.findByParqueaderoId(parqueaderoId).stream()
                .filter(config -> config.isCantidadFija() && tipo.equalsIgnoreCase(config.getTipo()))
                .findFirst()
                .ifPresent(config -> {
                    long ocupados = espacioRepository.countByParqueaderoIdAndTipo(parqueaderoId, config.getTipo());
                    if (config.getCantidad() == null || ocupados + nuevos > config.getCantidad()) {
                        throw new IllegalArgumentException(
                                "El tipo de vehículo " + config.getTipo() + " alcanzó su cupo fijo.");
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<TipoVehiculoResponse> listarTipos(String email, Long parqueaderoId) {
        requireParqueadero(parqueaderoId);
        accesoParqueaderoService.verificarAcceso(email, parqueaderoId);
        return tipoVehiculoRepository.findByParqueaderoId(parqueaderoId).stream()
                .map(config -> TipoVehiculoResponse.builder()
                        .id(config.getId())
                        .tipo(config.getTipo())
                        .cantidadFija(config.isCantidadFija())
                        .cantidad(config.getCantidad())
                        .build())
                .collect(Collectors.toList());
    }

    public Parqueadero requireParqueadero(Long id) {
        return parqueaderoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Parqueadero no encontrado con id: " + id));
    }

    private void verificarAccesoEmpresa(String emailActor, Long empresaId) {
        Usuario actor = accesoParqueaderoService.requireUsuario(emailActor);
        if (Roles.esPlataforma(actor.getRol())) {
            return;
        }
        if (!Roles.EMPRESA.equals(Roles.normalizar(actor.getRol()))
                || !empresaUsuarioRepository.existsByEmpresaIdAndUsuarioId(empresaId, actor.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "No tienes acceso a esta empresa.");
        }
    }

    private void validarTipos(List<TipoVehiculoRequest> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            return;
        }
        Set<String> vistos = new HashSet<>();
        for (TipoVehiculoRequest tipo : tipos) {
            String nombre = tipo.getTipo().trim().toUpperCase(Locale.ROOT);
            if (!vistos.add(nombre)) {
                throw new IllegalArgumentException("El tipo de vehículo " + nombre + " está repetido.");
            }
            if (tipo.isCantidadFija()
                    && (tipo.getCantidad() == null || tipo.getCantidad() <= 0)) {
                throw new IllegalArgumentException(
                        "El tipo " + nombre + " con cupo fijo debe indicar una cantidad mayor a cero.");
            }
        }
    }

    private List<ParqueaderoTipoVehiculo> guardarTipos(
            Long parqueaderoId, List<TipoVehiculoRequest> tipos) {
        if (tipos == null || tipos.isEmpty()) {
            return List.of();
        }
        List<ParqueaderoTipoVehiculo> entities = new ArrayList<>();
        for (TipoVehiculoRequest tipo : tipos) {
            entities.add(tipoVehiculoRepository.save(ParqueaderoTipoVehiculo.builder()
                    .parqueaderoId(parqueaderoId)
                    .tipo(tipo.getTipo().trim().toUpperCase(Locale.ROOT))
                    .cantidadFija(tipo.isCantidadFija())
                    .cantidad(tipo.getCantidad())
                    .build()));
        }
        return entities;
    }

    private Usuario crearCajero(Parqueadero parqueadero, ParqueaderoRequest request) {
        String email = request.getCajeroEmail().trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con el email del cajero: " + email);
        }

        Usuario cajero = usuarioRepository.save(Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getCajeroPassword()))
                .nombre(request.getCajeroNombre())
                .rol(Roles.CAJERO_PAGOS)
                .build());
        parqueaderoUsuarioRepository.save(ParqueaderoUsuario.builder()
                .parqueaderoId(parqueadero.getId())
                .usuarioId(cajero.getId())
                .rolAsignado(Roles.CAJERO_PAGOS)
                .build());
        return cajero;
    }

    private ParqueaderoResponse mapToResponse(
            Parqueadero parqueadero,
            List<ParqueaderoTipoVehiculo> tipos,
            String cajeroEmail) {
        List<ParqueaderoTipoVehiculo> configurados = tipos != null
                ? tipos
                : tipoVehiculoRepository.findByParqueaderoId(parqueadero.getId());
        List<TipoVehiculoResponse> tiposResponse = configurados.stream()
                .map(config -> TipoVehiculoResponse.builder()
                        .id(config.getId())
                        .tipo(config.getTipo())
                        .cantidadFija(config.isCantidadFija())
                        .cantidad(config.getCantidad())
                        .espaciosOcupados(espacioRepository.countByParqueaderoIdAndTipo(
                                parqueadero.getId(), config.getTipo()))
                        .build())
                .collect(Collectors.toList());

        if (cajeroEmail == null) {
            cajeroEmail = parqueaderoUsuarioRepository.findByParqueaderoId(parqueadero.getId()).stream()
                    .filter(vinculo -> Roles.CAJERO_PAGOS.equals(vinculo.getRolAsignado()))
                    .findFirst()
                    .flatMap(vinculo -> usuarioRepository.findById(vinculo.getUsuarioId()))
                    .map(Usuario::getEmail)
                    .orElse(null);
        }

        return ParqueaderoResponse.builder()
                .id(parqueadero.getId())
                .empresaId(parqueadero.getEmpresaId())
                .nombre(parqueadero.getNombre())
                .identificador(parqueadero.getIdentificador())
                .capacidadVehiculos(parqueadero.getCapacidadVehiculos())
                .activo(parqueadero.isActivo())
                .cajeroEmail(cajeroEmail)
                .tipos(tiposResponse)
                .fechaCreacion(parqueadero.getFechaCreacion())
                .build();
    }
}
