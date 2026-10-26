package com.parkflow360.security.service;

import com.parkflow360.security.dto.LoginRequest;
import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private Usuario testUsuario;

    @BeforeEach
    void setUp() {
        testUsuario = Usuario.builder()
                .id(1L)
                .email("admin@parkflow360.com")
                .password("$2a$10$encodedPassword")
                .rol("ADMIN")
                .nombre("Administrador")
                .build();
    }

    @Test
    void login_WithValidCredentials_ShouldReturnLoginResponse() {
        // Given
        LoginRequest request = new LoginRequest("admin@parkflow360.com", "admin123");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUsuario));
        when(passwordEncoder.matches(request.getPassword(), testUsuario.getPassword())).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyString(), anyString())).thenReturn("test-jwt-token");

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("test-jwt-token", response.getToken());
        assertEquals("Bearer", response.getTipo());
        assertEquals("admin@parkflow360.com", response.getEmail());
        assertEquals("ADMIN", response.getRol());
        assertEquals("Administrador", response.getNombre());
        
        verify(usuarioRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), testUsuario.getPassword());
        verify(jwtService).generateToken(testUsuario.getEmail(), testUsuario.getRol(), testUsuario.getNombre());
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowException() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent@parkflow360.com", "admin123");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> authService.login(request));
        verify(usuarioRepository).findByEmail(request.getEmail());
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Given
        LoginRequest request = new LoginRequest("admin@parkflow360.com", "wrongpassword");
        when(usuarioRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUsuario));
        when(passwordEncoder.matches(request.getPassword(), testUsuario.getPassword())).thenReturn(false);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authService.login(request));
        verify(usuarioRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), testUsuario.getPassword());
        verifyNoInteractions(jwtService);
    }
}