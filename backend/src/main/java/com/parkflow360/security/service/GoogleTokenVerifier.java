package com.parkflow360.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkflow360.security.dto.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Verifica el ID token emitido por Google Identity Services.
 * Se consulta el endpoint público tokeninfo de Google, que valida la firma y
 * la vigencia del token del lado de Google.
 */
@Service
public class GoogleTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${google.client-id:}")
    private String googleClientId;

    public GoogleUserInfo verificar(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("Token de Google vacío");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TOKENINFO_URL + URLEncoder.encode(idToken, StandardCharsets.UTF_8)))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new IllegalArgumentException("Token de Google inválido o expirado");
            }

            JsonNode json = objectMapper.readTree(response.body());

            String email = json.path("email").asText(null);
            String sub = json.path("sub").asText(null);
            String name = json.path("name").asText(null);
            boolean emailVerified = json.path("email_verified").asBoolean(false);

            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("El token de Google no contiene un correo válido");
            }
            if (!emailVerified) {
                throw new IllegalArgumentException("El correo de Google no está verificado");
            }
            // Si se configuró un client-id, el token debe haber sido emitido para él.
            String aud = json.path("aud").asText(null);
            if (googleClientId != null && !googleClientId.isBlank()
                    && aud != null && !googleClientId.equals(aud)) {
                throw new IllegalArgumentException("El token de Google no corresponde a esta aplicación");
            }

            return GoogleUserInfo.builder()
                    .sub(sub)
                    .email(email)
                    .name(name != null ? name : email)
                    .emailVerified(true)
                    .build();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo verificar el token de Google: " + e.getMessage());
        }
    }
}
