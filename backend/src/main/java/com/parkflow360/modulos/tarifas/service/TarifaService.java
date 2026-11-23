package com.parkflow360.modulos.tarifas.service;

import com.parkflow360.modulos.tarifas.dto.TarifaRequest;
import com.parkflow360.modulos.tarifas.dto.TarifaResponse;
import com.parkflow360.modulos.tarifas.entity.Tarifa;
import com.parkflow360.modulos.tarifas.repository.TarifaRepository;
import com.parkflow360.shared.service.AccesoParqueaderoService;
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
    private final AccesoParqueaderoService accesoParqueaderoService;

    @Transactional(readOnly = true)
    public List<TarifaResponse> listarTodos(String email) {
        if (accesoParqueaderoService.esPlataforma(email)) {
            return tarifaRepository.findAll().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        }
        List<Long> permitidos = accesoParqueaderoService.parqueaderosPermitidos(email);
        return tarifaRepository.findByParqueaderoIdIsNullOrParqueaderoIdIn(permitidos).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TarifaResponse> listarPorTipo(String email, String tipo) {
        return listarTodos(email).stream()
                .filter(tarifa -> tipo.equalsIgnoreCase(tarifa.getTipo()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TarifaResponse obtenerPorId(String email, Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarifa no encontrada con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, tarifa.getParqueaderoId());
        return mapToResponse(tarifa);
    }

    @Transactional
    public TarifaResponse crear(String email, TarifaRequest request) {
        Long parqueaderoId = request.getParqueaderoId();
        if (parqueaderoId == null && !accesoParqueaderoService.esPlataforma(email)) {
            parqueaderoId = accesoParqueaderoService.resolverParqueadero(email, null);
        } else {
            accesoParqueaderoService.verificarAcceso(email, parqueaderoId);
        }

        Tarifa tarifa = Tarifa.builder()
                .parqueaderoId(parqueaderoId)
                .nombre(request.getNombre())
                .tipo(request.getTipo())
                .valor(request.getValor())
                .duracion(request.getDuracion())
                .build();

        return mapToResponse(tarifaRepository.save(tarifa));
    }

    @Transactional
    public TarifaResponse actualizar(String email, Long id, TarifaRequest request) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarifa no encontrada con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, tarifa.getParqueaderoId());

        if (request.getParqueaderoId() != null
                && !request.getParqueaderoId().equals(tarifa.getParqueaderoId())) {
            tarifa.setParqueaderoId(accesoParqueaderoService.resolverParqueadero(
                    email, request.getParqueaderoId()));
        }
        tarifa.setNombre(request.getNombre());
        tarifa.setTipo(request.getTipo());
        tarifa.setValor(request.getValor());
        tarifa.setDuracion(request.getDuracion());

        return mapToResponse(tarifaRepository.save(tarifa));
    }

    @Transactional
    public void eliminar(String email, Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tarifa no encontrada con id: " + id));
        accesoParqueaderoService.verificarAcceso(email, tarifa.getParqueaderoId());
        tarifaRepository.deleteById(id);
    }

    private TarifaResponse mapToResponse(Tarifa tarifa) {
        return TarifaResponse.builder()
                .id(tarifa.getId())
                .parqueaderoId(tarifa.getParqueaderoId())
                .nombre(tarifa.getNombre())
                .tipo(tarifa.getTipo())
                .valor(tarifa.getValor())
                .duracion(tarifa.getDuracion())
                .fechaCreacion(tarifa.getFechaCreacion())
                .build();
    }
}
