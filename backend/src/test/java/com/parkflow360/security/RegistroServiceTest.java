package com.parkflow360.security;

import com.parkflow360.security.dto.RegistroRequest;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.service.RegistroService;
import com.parkflow360.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    JwtService jwtService;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    RegistroService registroService;

    @Test
    void rechazaEmailDuplicadoNormalizado() {
        RegistroRequest request = RegistroRequest.builder()
                .email(" EXISTENTE@EMAIL.COM ")
                .nombre("Usuario")
                .password("password123")
                .build();
        when(usuarioRepository.existsByEmail("existente@email.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> registroService.registrar(request));
        verify(usuarioRepository, never()).save(any());
    }
}
