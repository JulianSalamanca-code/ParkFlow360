package com.parkflow360.modulos.espacios.service;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspacioService {

    private final EspacioRepository espacioRepository;
    private final IngresoRepository ingresoRepository;
    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    public List<EspacioResponse> listarTodos() {
        return espacioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EspacioResponse> listarPorEstado(String estado) {
        return espacioRepository.findByEstado(estado).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EspacioResponse obtenerPorId(Long id) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado con id: " + id));
        return mapToResponse(espacio);
    }

    @Transactional
    public EspacioResponse crear(EspacioRequest request) {
        if (espacioRepository.existsByNumero(request.getNumero())) {
            throw new IllegalArgumentException("Ya existe un espacio con el número: " + request.getNumero());
        }

        Espacio espacio = Espacio.builder()
                .numero(request.getNumero())
                .tipo(request.getTipo())
                .estado(request.getEstado())
                .piso(request.getPiso())
                .build();

        Espacio guardado = espacioRepository.save(espacio);
        return mapToResponse(guardado);
    }

    @Transactional
    public List<EspacioResponse> importar(List<EspacioRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Debes incluir al menos un espacio para importar.");
        }
        if (requests.size() > 2000) {
            throw new IllegalArgumentException("No se pueden importar más de 2000 espacios por lote.");
        }

        Set<String> numeros = new HashSet<>();
        List<Espacio> espacios = requests.stream()
                .map(request -> {
                    if (request == null || request.getNumero() == null || request.getNumero().isBlank()) {
                        throw new IllegalArgumentException("Todos los espacios deben tener un número.");
                    }

                    String numero = request.getNumero().trim();
                    String numeroNormalizado = numero.toUpperCase(Locale.ROOT);
                    if (!numeros.add(numeroNormalizado)) {
                        throw new IllegalArgumentException("El lote contiene el número repetido: " + numero);
                    }

                    return Espacio.builder()
                            .numero(numero)
                            .tipo(request.getTipo())
                            .estado(request.getEstado())
                            .piso(request.getPiso())
                            .build();
                })
                .collect(Collectors.toList());

        List<String> numerosNormalizados = numeros.stream().toList();
        List<Espacio> existentes = espacioRepository.findAllByNumeroIgnoreCaseIn(numerosNormalizados);
        if (!existentes.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ya existe un espacio con el número: " + existentes.get(0).getNumero());
        }

        return espacioRepository.saveAll(espacios).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EspacioResponse actualizar(Long id, EspacioRequest request) {
        Espacio espacio = espacioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Espacio no encontrado con id: " + id));

        espacio.setNumero(request.getNumero());
        espacio.setTipo(request.getTipo());
        espacio.setEstado(request.getEstado());
        espacio.setPiso(request.getPiso());

        Espacio actualizado = espacioRepository.save(espacio);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!espacioRepository.existsById(id)) {
            throw new NoSuchElementException("Espacio no encontrado con id: " + id);
        }

        // No eliminar si tiene una reserva o parqueo activo
        if (ingresoRepository.existsByEspacioIdAndEstadoIn(id, List.of("RESERVADO", "ACTIVO"))) {
            throw new IllegalArgumentException(
                "El espacio tiene una reserva o parqueo activo. Libera la reserva antes de eliminarlo.");
        }

        // No eliminar si tiene pagos asociados (integridad referencial)
        if (!pagoRepository.findByEspacioId(id).isEmpty()) {
            throw new IllegalArgumentException(
                "No se puede eliminar el espacio porque tiene pagos asociados en el historial.");
        }

        espacioRepository.deleteById(id);
    }

    private EspacioResponse mapToResponse(Espacio espacio) {
        return EspacioResponse.builder()
                .id(espacio.getId())
                .numero(espacio.getNumero())
                .tipo(espacio.getTipo())
                .estado(espacio.getEstado())
                .piso(espacio.getPiso())
                .fechaCreacion(espacio.getFechaCreacion())
                .build();
    }
}
