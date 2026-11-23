package com.parkflow360.security.controller;

import com.parkflow360.security.dto.GoogleLoginRequest;
import com.parkflow360.security.dto.LoginRequest;
import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.dto.RegistroRequest;
import com.parkflow360.security.dto.SolicitarRecuperacionRequest;
import com.parkflow360.security.dto.RestablecerPasswordRequest;
import com.parkflow360.security.service.AuthService;
import com.parkflow360.security.service.GoogleAuthService;
import com.parkflow360.security.service.RegistroService;
import com.parkflow360.security.service.RecuperacionPasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final RegistroService registroService;
    private final RecuperacionPasswordService recuperacionPasswordService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.ok(registroService.registrar(request));
    }

    @PostMapping("/recuperacion/solicitar")
    public ResponseEntity<Void> solicitarRecuperacion(
            @Valid @RequestBody SolicitarRecuperacionRequest request) {
        recuperacionPasswordService.solicitar(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/recuperacion/restablecer")
    public ResponseEntity<Void> restablecerPassword(
            @Valid @RequestBody RestablecerPasswordRequest request) {
        recuperacionPasswordService.restablecer(request);
        return ResponseEntity.noContent().build();
    }

    /** Login con el ID token (credential) emitido por Google Identity Services. */
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> loginConGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.loginConGoogle(request));
    }

    @GetMapping("/google/authorize")
    public ResponseEntity<Void> googleAuthorize(@RequestParam String state) {
        String authorizationUrl = googleAuthService.getAuthorizationUrl(state);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, authorizationUrl)
                .build();
    }

    @GetMapping("/google/callback")
    @ResponseBody
    public ResponseEntity<LoginResponse> googleCallback(@RequestParam String code) {
        return ResponseEntity.ok(googleAuthService.handleGoogleCallback(code));
    }
}
