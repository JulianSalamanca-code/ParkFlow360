package com.parkflow360.security.service;

import com.parkflow360.security.dto.RestablecerPasswordRequest;
import com.parkflow360.security.dto.SolicitarRecuperacionRequest;
import com.parkflow360.security.entity.PasswordResetToken;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.PasswordResetTokenRepository;
import com.parkflow360.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RecuperacionPasswordService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${MAIL_HOST:}")
    private String mailHost;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${app.password-reset-expiration-minutes:30}")
    private int expirationMinutes;

    @Transactional
    public void solicitar(SolicitarRecuperacionRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            resetTokenRepository.deleteByUsuario(usuario);
            resetTokenRepository.flush();

            String token = generarToken();
            PasswordResetToken registro = PasswordResetToken.builder()
                    .usuario(usuario)
                    .tokenHash(hash(token))
                    .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                    .build();
            resetTokenRepository.save(registro);

            if (mailHost != null && !mailHost.isBlank()) {
                enviarCorreo(usuario.getEmail(), token);
            }
        });
    }

    @Transactional
    public void restablecer(RestablecerPasswordRequest request) {
        PasswordResetToken registro = resetTokenRepository.findByTokenHash(hash(request.getToken()))
                .orElseThrow(() -> new IllegalArgumentException("El enlace de recuperación no es válido"));

        if (registro.getUsedAt() != null) {
            throw new IllegalArgumentException("El enlace de recuperación ya fue utilizado");
        }
        if (registro.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("El enlace de recuperación venció");
        }

        Usuario usuario = registro.getUsuario();
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setProveedor(usuario.getProveedor() == null ? "LOCAL" : usuario.getProveedor());
        usuarioRepository.save(usuario);

        registro.setUsedAt(LocalDateTime.now());
        resetTokenRepository.save(registro);
    }

    private void enviarCorreo(String email, String token) {
        String enlace = frontendUrl.replaceAll("/$", "")
                + "/auth/restablecer/" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(email);
        mensaje.setSubject("Recuperación de contraseña - ParkFlow360");
        mensaje.setText("Restablece tu contraseña usando este enlace:\n\n"
                + enlace + "\n\nEl enlace expira en " + expirationMinutes + " minutos.");
        mailSender.send(mensaje);
    }

    private String generarToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo proteger el token de recuperación", ex);
        }
    }
}
