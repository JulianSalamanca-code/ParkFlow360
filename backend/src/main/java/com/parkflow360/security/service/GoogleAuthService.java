package com.parkflow360.security.service;

import com.parkflow360.security.dto.LoginResponse;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import com.parkflow360.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${app.oauth2.redirect-uri:http://localhost:4200/auth/google/callback}")
    private String redirectUri;

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

    public LoginResponse handleGoogleCallback(String code) {
        // 1. Intercambiar código por access token
        String accessToken = exchangeCodeForToken(code);
        
        // 2. Obtener información del usuario de Google
        Map<String, Object> userInfo = getUserInfo(accessToken);
        
        String email = (String) userInfo.get("email");
        String nombre = (String) userInfo.get("name");
        String googleId = (String) userInfo.get("sub");

        // 3. Buscar o crear usuario
        return loginWithGoogle(email, nombre, googleId);
    }

    private String exchangeCodeForToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }
        
        throw new RuntimeException("Error al obtener access token de Google");
    }

    private Map<String, Object> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUri, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }
        
        throw new RuntimeException("Error al obtener información del usuario de Google");
    }
}