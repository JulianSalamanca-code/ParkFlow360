package com.parkflow360.modulos.vehiculos.service;

import com.parkflow360.modulos.vehiculos.dto.VehiculoRequest;
import com.parkflow360.modulos.vehiculos.dto.VehiculoResponse;
import com.parkflow360.modulos.vehiculos.entity.Vehiculo;
import com.parkflow360.modulos.vehiculos.repository.VehiculoRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    // ---------- Administrador ----------

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

        return mapToResponse(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoResponse actualizar(Long id, VehiculoRequest request) {
        Vehiculo vehiculo = vehiculoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vehículo no encontrado con id: " + id));

        vehiculo.setPlaca(request.getPlaca());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setColor(request.getColor());
        vehiculo.setModelo(request.getModelo());

        return mapToResponse(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!vehiculoRepository.existsById(id)) {
            throw new NoSuchElementException("Vehículo no encontrado con id: " + id);
        }
        vehiculoRepository.deleteById(id);
    }

    // ---------- Usuario (cliente) ----------

    @Transactional(readOnly = true)
    public List<VehiculoResponse> listarPorUsuarioEmail(String email) {
        Usuario usuario = requireUsuario(email);
        return vehiculoRepository.findByUsuarioId(usuario.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehiculoResponse crearParaUsuario(String email, VehiculoRequest request) {
        Usuario usuario = requireUsuario(email);

        if (vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + request.getPlaca());
        }

        Vehiculo vehiculo = Vehiculo.builder()
                .placa(request.getPlaca())
                .tipo(request.getTipo())
                .color(request.getColor())
                .modelo(request.getModelo())
                .usuarioId(usuario.getId())
                .build();

        return mapToResponse(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public VehiculoResponse actualizarParaUsuario(String email, Long id, VehiculoRequest request) {
        Usuario usuario = requireUsuario(email);
        Vehiculo vehiculo = vehiculoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new NoSuchElementException("Vehículo no encontrado entre tus vehículos: " + id));

        if (!vehiculo.getPlaca().equalsIgnoreCase(request.getPlaca())
                && vehiculoRepository.existsByPlaca(request.getPlaca())) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + request.getPlaca());
        }

        vehiculo.setPlaca(request.getPlaca());
        vehiculo.setTipo(request.getTipo());
        vehiculo.setColor(request.getColor());
        vehiculo.setModelo(request.getModelo());

        return mapToResponse(vehiculoRepository.save(vehiculo));
    }

    @Transactional
    public void eliminarParaUsuario(String email, Long id) {
        Usuario usuario = requireUsuario(email);
        Vehiculo vehiculo = vehiculoRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new NoSuchElementException("Vehículo no encontrado entre tus vehículos: " + id));
        vehiculoRepository.delete(vehiculo);
    }

    private Usuario requireUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + email));
    }

    private VehiculoResponse mapToResponse(Vehiculo vehiculo) {
        return VehiculoResponse.builder()
                .id(vehiculo.getId())
                .placa(vehiculo.getPlaca())
                .tipo(vehiculo.getTipo())
                .color(vehiculo.getColor())
                .modelo(vehiculo.getModelo())
                .usuarioId(vehiculo.getUsuarioId())
                .fechaCreacion(vehiculo.getFechaCreacion())
                .build();
    }
}
