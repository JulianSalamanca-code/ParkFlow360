package com.parkflow360.security.service;

import com.parkflow360.security.dto.LoginRequest;
import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

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
