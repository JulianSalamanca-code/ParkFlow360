package com.parkflow360.security;

import com.parkflow360.security.dto.RestablecerPasswordRequest;
import com.parkflow360.security.entity.PasswordResetToken;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.PasswordResetTokenRepository;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.service.RecuperacionPasswordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecuperacionPasswordServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    PasswordResetTokenRepository resetTokenRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JavaMailSender mailSender;

    @InjectMocks
    RecuperacionPasswordService service;

    @Test
    void rechazaTokenUsado() throws Exception {
        PasswordResetToken registro = PasswordResetToken.builder()
                .usuario(Usuario.builder().email("usuario@ejemplo.com").build())
                .usedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        RestablecerPasswordRequest request = RestablecerPasswordRequest.builder()
                .token("token")
                .password("password123")
                .build();

        Method hash = RecuperacionPasswordService.class.getDeclaredMethod("hash", String.class);
        hash.setAccessible(true);
        String hashed = (String) hash.invoke(service, "token");
        when(resetTokenRepository.findByTokenHash(hashed)).thenReturn(Optional.of(registro));

        assertThrows(IllegalArgumentException.class,
                () -> service.restablecer(request));
    }
}
