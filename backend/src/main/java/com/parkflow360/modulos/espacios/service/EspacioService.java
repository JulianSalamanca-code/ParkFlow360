package com.parkflow360.modulos.espacios.service;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.modulos.parqueaderos.service.ParqueaderoService;
import com.parkflow360.shared.service.AccesoParqueaderoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspacioService {

    private final EspacioRepository espacioRepository;
    private final IngresoRepository ingresoRepository;
    private final PagoRepository pagoRepository;
    private final AccesoParqueaderoService accesoParqueaderoService;
    private final ParqueaderoService parqueaderoService;

    @Transactional(readOnly = true)
    public List<EspacioResponse> listarTodos(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return espacioRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        List<Long> permitidos = accesoParqueaderoService.parqueaderosPermitidos(email);
        if (permitidos.isEmpty()) {
            return List.of();
        }
        return espacioRepository.findByParqueaderoIdIn(permitidos).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EspacioResponse> listarPorEstado(String email, String estado) {
        return listarTodos(email).stream()
                .filter(espacio -> estado.equalsIgnoreCase(espacio.getEstado()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EspacioResponse obtenerPorId(String email, Long id) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, espacio.getParqueaderoId());
        return mapToResponse(espacio);
    }

    @Transactional
    public EspacioResponse crear(String email, EspacioRequest request) {
        Long parqueaderoId = accesoParqueaderoService.resolverParqueadero(
                email, request.getParqueaderoId());
        String numero = normalizarNumero(request.getNumero());
        String tipo = normalizarTipo(request.getTipo());
        validarNumeroDisponible(parqueaderoId, numero);
        parqueaderoService.validarCupo(parqueaderoId, tipo);

        Espacio espacio = Espacio.builder()
                .parqueaderoId(parqueaderoId)
                .numero(numero)
                .tipo(tipo)
                .estado(request.getEstado())
                .piso(request.getPiso())
                .planoId(request.getPlanoId())
                .fila(request.getFila())
                .columna(request.getColumna())
                .posX(request.getPosX())
                .posY(request.getPosY())
                .ancho(request.getAncho())
                .alto(request.getAlto())
                .build();

        return mapToResponse(espacioRepository.save(espacio));
    }

    @Transactional
    public List<EspacioResponse> importar(String email, List<EspacioRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Debes incluir al menos un espacio para importar.");
        }
        if (requests.size() > 2000) {
            throw new IllegalArgumentException("No se pueden importar más de 2000 espacios por lote.");
        }

        Long parqueaderoSolicitado = requests.stream()
                .map(EspacioRequest::getParqueaderoId)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(null);
        Long parqueaderoId = accesoParqueaderoService.resolverParqueadero(email, parqueaderoSolicitado);

        Set<String> numeros = new HashSet<>();
        Map<String, Integer> solicitadosPorTipo = new HashMap<>();
        List<Espacio> espacios = requests.stream()
                .map(request -> {
                    if (request == null || request.getNumero() == null || request.getNumero().isBlank()) {
                        throw new IllegalArgumentException("Todos los espacios deben tener un número.");
                    }
                    if (request.getParqueaderoId() != null
                            && !parqueaderoId.equals(request.getParqueaderoId())) {
                        throw new IllegalArgumentException(
                                "Todos los espacios del lote deben pertenecer al mismo parqueadero.");
                    }

                    String numero = normalizarNumero(request.getNumero());
                    if (!numeros.add(numero)) {
                        throw new IllegalArgumentException(
                                "El lote contiene el número repetido: " + request.getNumero());
                    }
                    String tipo = normalizarTipo(request.getTipo());
                    int solicitados = solicitadosPorTipo.merge(
                            tipo == null ? "" : tipo, 1, Integer::sum);
                    parqueaderoService.validarCupo(parqueaderoId, tipo, solicitados);

                    return Espacio.builder()
                            .parqueaderoId(parqueaderoId)
                            .numero(numero)
                            .tipo(tipo)
                            .estado(request.getEstado())
                            .piso(request.getPiso())
                            .build();
                })
                .collect(Collectors.toList());

        for (String numero : numeros) {
            validarNumeroDisponible(parqueaderoId, numero);
        }

        return espacioRepository.saveAll(espacios).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EspacioResponse actualizar(String email, Long id, EspacioRequest request) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, espacio.getParqueaderoId());

        String numero = normalizarNumero(request.getNumero());
        String tipo = normalizarTipo(request.getTipo());

        Long parqueaderoId = espacio.getParqueaderoId();
        if (request.getParqueaderoId() != null
                && !request.getParqueaderoId().equals(espacio.getParqueaderoId())) {
            parqueaderoId = accesoParqueaderoService.resolverParqueadero(email, request.getParqueaderoId());
            parqueaderoService.validarCupo(parqueaderoId, tipo);
        }

        if (!espacio.getNumero().equalsIgnoreCase(numero)
                || !parqueaderoId.equals(espacio.getParqueaderoId())) {
            validarNumeroDisponible(parqueaderoId, numero);
        }

        espacio.setParqueaderoId(parqueaderoId);
        espacio.setNumero(numero);
        espacio.setTipo(tipo);
        espacio.setEstado(request.getEstado());
        espacio.setPiso(request.getPiso());
        if (request.getPlanoId() != null) espacio.setPlanoId(request.getPlanoId());
        if (request.getFila() != null) espacio.setFila(request.getFila());
        if (request.getColumna() != null) espacio.setColumna(request.getColumna());
        if (request.getPosX() != null) espacio.setPosX(request.getPosX());
        if (request.getPosY() != null) espacio.setPosY(request.getPosY());
        if (request.getAncho() != null) espacio.setAncho(request.getAncho());
        if (request.getAlto() != null) espacio.setAlto(request.getAlto());

        return mapToResponse(espacioRepository.save(espacio));
    }

    @Transactional
    public void eliminar(String email, Long id) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, espacio.getParqueaderoId());

        if (ingresoRepository.existsByEspacioIdAndEstadoIn(id, List.of("RESERVADO", "ACTIVO"))) {
            throw new IllegalArgumentException(
                    "El espacio tiene una reserva o parqueo activo. Libera la reserva antes de eliminarlo.");
        }
        if (!pagoRepository.findByEspacioId(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el espacio porque tiene pagos asociados en el historial.");
        }
        espacioRepository.deleteById(id);
    }

    private void validarNumeroDisponible(Long parqueaderoId, String numero) {
        if (espacioRepository.existsByParqueaderoIdAndNumeroIgnoreCase(parqueaderoId, numero)) {
            throw new IllegalArgumentException(
                    "Ya existe un espacio con el número " + numero + " en este parqueadero.");
        }
    }

    private String normalizarNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El número del espacio es obligatorio.");
        }
        return numero.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarTipo(String tipo) {
        return tipo == null ? null : tipo.trim().toUpperCase(Locale.ROOT);
    }

    private EspacioResponse mapToResponse(Espacio espacio) {
        return EspacioResponse.builder()
                .id(espacio.getId())
                .parqueaderoId(espacio.getParqueaderoId())
                .numero(espacio.getNumero())
                .tipo(espacio.getTipo())
                .estado(espacio.getEstado())
                .piso(espacio.getPiso())
                .planoId(espacio.getPlanoId())
                .fila(espacio.getFila())
                .columna(espacio.getColumna())
                .posX(espacio.getPosX())
                .posY(espacio.getPosY())
                .ancho(espacio.getAncho())
                .alto(espacio.getAlto())
                .fechaCreacion(espacio.getFechaCreacion())
                .build();
    }
}
