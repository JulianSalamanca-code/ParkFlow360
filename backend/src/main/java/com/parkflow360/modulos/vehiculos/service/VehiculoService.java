package com.parkflow360.modulos.vehiculos.service;

import com.parkflow360.modulos.ingresos.repository.IngresoRepository;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
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

/**
 * El registro y edición de vehículos es exclusivo del cliente (rol USUARIO)
 * y siempre limitado a los suyos. El ADMIN solo lee el catálogo.
 */
@Service
@RequiredArgsConstructor
public class VehiculoService {

    private final VehiculoRepository vehiculoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoRepository pagoRepository;
    private final IngresoRepository ingresoRepository;

    // ---------- Administrador (solo lectura) ----------

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

    // ---------- Cliente (rol USUARIO) ----------

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
                .placa(request.getPlaca().toUpperCase())
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

        String nuevaPlaca = request.getPlaca().toUpperCase();
        if (!vehiculo.getPlaca().equalsIgnoreCase(nuevaPlaca)
                && vehiculoRepository.existsByPlaca(nuevaPlaca)) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa: " + nuevaPlaca);
        }

        vehiculo.setPlaca(nuevaPlaca);
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

        // Se valida antes de borrar para devolver un mensaje claro y no un error de base de datos.
        if (!pagoRepository.findByVehiculoId(id).isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el vehículo porque tiene pagos registrados en el historial.");
        }
        if (ingresoRepository.existsByVehiculoIdAndEstadoIn(id, List.of("ACTIVO", "RESERVADO"))) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el vehículo porque tiene un parqueo o reserva activa.");
        }

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
