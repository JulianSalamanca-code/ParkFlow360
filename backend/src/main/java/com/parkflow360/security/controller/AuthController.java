package com.parkflow360.security.controller;

import com.parkflow360.security.dto.GoogleLoginRequest;
import com.parkflow360.security.dto.LoginRequest;
import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.dto.RegistroRequest;
import com.parkflow360.security.service.AuthService;
import com.parkflow360.security.service.GoogleAuthService;
import com.parkflow360.security.service.RegistroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final RegistroService registroService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(registroService.registrar(request));
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(googleAuthService.loginWithGoogle(
                request.getEmail(),
                request.getNombre(),
                request.getGoogleId()
        ));
    }

    @GetMapping("/google/callback")
    public ResponseEntity<LoginResponse> googleCallback(@RequestParam String code) {
        return ResponseEntity.ok(googleAuthService.handleGoogleCallback(code));
    }
}
