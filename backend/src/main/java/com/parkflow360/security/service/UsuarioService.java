package com.parkflow360.security.service;

import com.parkflow360.security.dto.UsuarioRequest;
import com.parkflow360.security.dto.UsuarioResponse;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));
        return mapToResponse(usuario);
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + request.getEmail());
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria para crear un usuario");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .nombre(request.getNombre())
                .build();

        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con id: " + id));

        usuario.setEmail(request.getEmail());
        usuario.setRol(request.getRol());
        usuario.setNombre(request.getNombre());

        // Solo cambia la contraseña si se envía una nueva
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return mapToResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new NoSuchElementException("Usuario no encontrado con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponse mapToResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .nombre(usuario.getNombre())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}
