package com.parkflow360.security.service;

import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public LoginResponse loginWithGoogle(String email, String nombre, String googleId) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    Usuario nuevo = Usuario.builder()
                            .email(email)
                            .nombre(nombre)
                            .googleId(googleId)
                            .rol("USUARIO")
                            .build();
                    return usuarioRepository.save(nuevo);
                });

        // Si el usuario existe pero no tiene googleId, actualizarlo
        if (usuario.getGoogleId() == null) {
            usuario.setGoogleId(googleId);
            usuarioRepository.save(usuario);
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