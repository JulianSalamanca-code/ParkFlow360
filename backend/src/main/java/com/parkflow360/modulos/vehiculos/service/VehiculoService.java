package com.parkflow360.modulos.vehiculos.service;

import com.parkflow360.modulos.vehiculos.dto.VehiculoRequest;
import com.parkflow360.modulos.vehiculos.dto.VehiculoResponse;
import com.parkflow360.modulos.vehiculos.entity.Vehiculo;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;

    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarTodos() {
        return vehiculoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehiculoResponse obtenerPorId(Long id) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vehículo no encontrado con id: " + id));
        return mapToResponse(vehiculo);
    }

    @Transactional
    public VehiculoResponse crear(VehiculoRequest request) {
        if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + request.getPlaca());
        }

        Vehiculo vehiculo = Vehiculo.builder()
                .placa(request.getPlaca())
                .tipo(request.getTipo())
                .color(request.getColor())
                .modelo(request.getModelo())
                .build();

        Vehiculo guardado = vehiculoRepository.save(vehiculo);
        return mapToResponse(guardado);
    }

    @Transactional
    public VehiculoResponse actualizar(Long id, VehiculoRequest request) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vehículo no encontrado con id: " + id));

        vehiculo.setPlaca(request.getPlaca());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setColor(request.getColor());
        vehiculo.setModelo(request.getModelo());

        Vehiculo actualizado = vehiculoRepository.save(vehiculo);
        return mapToResponse(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new NoSuchElementException("Vehículo no encontrado con id: " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    private VehiculoResponse mapToResponse(Vehiculo vehiculo) {
        return VehiculoResponse.builder()
                .id(vehiculo.getId())
                .placa(vehiculo.getPlaca())
                .tipo(vehiculo.getTipo())
                .color(vehiculo.getColor())
                .modelo(vehiculo.getModelo())
                .fechaCreacion(vehiculo.getFechaCreacion())
                .build();
    }
}
