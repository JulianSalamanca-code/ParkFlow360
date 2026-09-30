package com.parkflow360.modulos.espacios.service;

import com.parkflow360.modulos.espacios.dto.EspacioRequest;
import com.parkflow360.modulos.espacios.dto.EspacioResponse;
import com.parkflow360.modulos.espacios.entity.Espacio;
import com.parkflow360.modulos.espacios.repository.EspacioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EspacioService {

    private final EspacioRepository espacioRepository;

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
