package com.parkflow360.security.service;

import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.dto.RegistroRequest;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegistroService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nombre(request.getNombre())
                .rol("USUARIO")
                .build();

        usuario = usuarioRepository.save(usuario);

        String token = jwtService.generateToken(usuario.getEmail(), usuario.getRol(), usuario.getNombre());

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .email(usuario.getEmail())
                .rol(usuario.getRol())
                .nombre(usuario.getNombre())
                .build();
    }
}