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
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
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

    @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${app.oauth2.redirect-uri:http://localhost:4200/auth/google/callback}")
    private String redirectUri;

    public String getAuthorizationUrl(String state) {
        if (clientId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El inicio de sesión con Google no está configurado en el servidor.");
        }
        if (state == null || !state.matches("[A-Za-z0-9_-]{32,128}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El estado OAuth no es válido.");
        }

        return UriComponentsBuilder.fromUriString(authorizationUri)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("state", state)
                .queryParam("prompt", "select_account")
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }

    private LoginResponse loginWithVerifiedGoogleUser(String email, String nombre, String googleId) {
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
        Map<?, ?> userInfo = getUserInfo(accessToken);
        
        String email = requiredUserInfoValue(userInfo, "email");
        String nombre = requiredUserInfoValue(userInfo, "name");
        String googleId = requiredUserInfoValue(userInfo, "sub");
        if (!Boolean.TRUE.equals(userInfo.get("email_verified"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google no verificó el correo de esta cuenta.");
        }

        // 3. Buscar o crear usuario
        return loginWithVerifiedGoogleUser(email, nombre, googleId);
    }

    private String exchangeCodeForToken(String code) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El client ID y el secreto de Google deben configurarse en el servidor.");
        }
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google no envió un código de autorización.");
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Object accessToken = response.getBody().get("access_token");
                if (accessToken instanceof String token && !token.isBlank()) {
                    return token;
                }
            }
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Google rechazó el código. Verifica que el client ID, el secreto y la URL de retorno coincidan.",
                    ex);
        }

        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google no devolvió un token de acceso válido.");
    }

    private Map<?, ?> getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    userInfoUri, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            }
        } catch (RestClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo verificar el usuario con Google.", ex);
        }

        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Google no devolvió información válida del usuario.");
    }

    private String requiredUserInfoValue(Map<?, ?> userInfo, String key) {
        Object value = userInfo.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google no devolvió los datos de cuenta requeridos.");
    }
}