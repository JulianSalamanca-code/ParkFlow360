package com.parkflow360.modulos.tarifas.service;

import com.parkflow360.modulos.tarifas.dto.TarifaRequest;
import com.parkflow360.modulos.tarifas.dto.TarifaResponse;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    @Transactional(readOnly = true)
    public List<TarifaResponse> listarTodos() {
        return tarifaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TarifaResponse> listarPorTipo(String tipo) {
        return tarifaRepository.findByTipo(tipo).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TarifaResponse obtenerPorId(Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarifa no encontrada con id: " + id));
        return mapToResponse(tarifa);
    }

    @Transactional
    public TarifaResponse crear(TarifaRequest request) {
        Tarifa tarifa = Tarifa.builder()
                .nombre(request.getNombre())
                .tipo(request.getTipo())
                .valor(request.getValor())
                .duracion(request.getDuracion())
                .build();

        Tarifa guardado = tarifaRepository.save(tarifa);
        return mapToResponse(guardado);
    }

    @Transactional
    public TarifaResponse actualizar(Long id, TarifaRequest request) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarifa no encontrada con id: " + id));

        tarifa.setNombre(request.getNombre());
        tarifa.setTipo(request.getTipo());
        tarifa.setValor(request.getValor());
        tarifa.setDuracion(request.getDuracion());

        Tarifa actualizado = tarifaRepository.save(tarifa);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!tarifaRepository.existsById(id)) {
            throw new NoSuchElementException("Tarifa no encontrada con id: " + id);
        }
        tarifaRepository.deleteById(id);
    }

    private TarifaResponse mapToResponse(Tarifa tarifa) {
        return TarifaResponse.builder()
                .id(tarifa.getId())
                .nombre(tarifa.getNombre())
                .tipo(tarifa.getTipo())
                .valor(tarifa.getValor())
                .duracion(tarifa.getDuracion())
                .fechaCreacion(tarifa.getFechaCreacion())
                .build();
    }
}
