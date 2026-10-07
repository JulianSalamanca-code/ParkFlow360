package com.parkflow360.security.service;

import com.parkflow360.security.dto.GoogleLoginRequest;
import com.parkflow360.security.dto.GoogleUserInfo;
import com.parkflow360.security.dto.LoginRequest;
import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final GoogleTokenVerifier googleTokenVerifier;

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        if (usuario.getPassword() == null) {
            throw new IllegalArgumentException(
                    "Esta cuenta usa inicio de sesión con Google. Ingresa con Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        return buildResponse(usuario);
    }

    /**
     * Inicia sesión (o registra) con una cuenta de Google.
     * La primera vez crea el usuario con rol USUARIO.
     */
    @Transactional
    public LoginResponse loginConGoogle(GoogleLoginRequest request) {
        GoogleUserInfo info = googleTokenVerifier.verificar(request.getCredential());

        Usuario usuario = usuarioRepository.findByEmail(info.getEmail())
                .map(existente -> {
                    // Vincula la cuenta local con Google la primera vez.
                    if (existente.getGoogleId() == null) {
                        existente.setGoogleId(info.getSub());
                        existente.setProveedor("GOOGLE");
                    }
                    if (existente.getNombre() == null || existente.getNombre().isBlank()) {
                        existente.setNombre(info.getName());
                    }
                    return usuarioRepository.save(existente);
                })
                .orElseGet(() -> usuarioRepository.save(Usuario.builder()
                        .email(info.getEmail())
                        .nombre(info.getName())
                        .rol("USUARIO")
                        .proveedor("GOOGLE")
                        .googleId(info.getSub())
                        .build()));

        return buildResponse(usuario);
    }

    private LoginResponse buildResponse(Usuario usuario) {
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
