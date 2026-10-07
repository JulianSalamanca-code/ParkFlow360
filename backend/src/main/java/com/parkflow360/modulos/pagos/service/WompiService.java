package com.parkflow360.modulos.pagos.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkflow360.modulos.pagos.dto.PagoOnlineRequest;
import com.parkflow360.modulos.pagos.dto.PagoOnlineResponse;
import com.parkflow360.modulos.pagos.dto.PagoResponse;
import com.parkflow360.modulos.pagos.entity.Pago;
import com.parkflow360.modulos.pagos.repository.PagoRepository;
import com.parkflow360.security.entity.Usuario;
import com.parkflow360.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Integración con Wompi (Bancolombia) para pagos en línea.
 * Flujo: se crea una transacción PENDIENTE, se genera la URL del checkout con
 * la firma de integridad, y el webhook confirma el resultado del pago.
 */
@Service
@RequiredArgsConstructor
public class WompiService {

    private final PagoRepository pagoRepository;
    private final PagoService pagoService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${wompi.public-key:}")
    private String publicKey;

    @Value("${wompi.integrity-secret:}")
    private String integritySecret;

    @Value("${wompi.events-secret:}")
    private String eventsSecret;

    @Value("${wompi.checkout-url:https://checkout.wompi.co/p/}")
    private String checkoutUrl;

    @Value("${wompi.currency:COP}")
    private String currency;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public PagoOnlineResponse crearPagoOnline(String email, PagoOnlineRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado: " + email));

        long valorEnCentavos = request.getValor()
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        String referencia = "PF-" + System.currentTimeMillis() + "-" + usuario.getId();

        Pago pago = Pago.builder()
                .valor(request.getValor())
                .estado("PENDIENTE")
                .referencia(referencia)
                .moneda(currency)
                .metodoPago("WOMPI")
                .usuarioId(usuario.getId())
                .ingresoId(request.getIngresoId())
                .vehiculoId(request.getVehiculoId())
                .espacioId(request.getEspacioId())
                .tarifaId(request.getTarifaId())
                .build();

        Pago guardado = pagoRepository.save(pago);

        String url = buildCheckoutUrl(referencia, valorEnCentavos);

        return PagoOnlineResponse.builder()
                .pagoId(guardado.getId())
                .referencia(referencia)
                .checkoutUrl(url)
                .valor(request.getValor())
                .valorEnCentavos(valorEnCentavos)
                .moneda(currency)
                .estado("PENDIENTE")
                .build();
    }

    /**
     * Procesa la notificación (webhook) de Wompi. Verifica la firma y actualiza
     * el estado del pago correspondiente.
     */
    @Transactional
    public void procesarWebhook(String rawPayload) {
        try {
            JsonNode event = objectMapper.readTree(rawPayload);
            String eventType = event.path("event").asText("");
            JsonNode transaction = event.path("data").path("transaction");

            if (!"transaction.updated".equals(eventType) || transaction.isMissingNode()) {
                return; // evento no relacionado con transacciones
            }

            if (!firmaValida(event)) {
                throw new IllegalArgumentException("Firma del webhook de Wompi inválida");
            }

            String referencia = transaction.path("reference").asText(null);
            if (referencia == null) {
                return;
            }

            Optional<Pago> opt = pagoRepository.findByReferencia(referencia);
            if (opt.isEmpty()) {
                return;
            }

            Pago pago = opt.get();
            String status = transaction.path("status").asText("");
            pago.setWompiTransactionId(transaction.path("id").asText(null));
            pago.setEstado(mapEstado(status));
            pagoRepository.save(pago);

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("No se pudo procesar el webhook de Wompi: " + e.getMessage());
        }
    }

    public PagoResponse obtenerPago(Long id) {
        return pagoService.obtenerPorId(id);
    }

    // ---------- Helpers ----------

    private String buildCheckoutUrl(String referencia, long valorEnCentavos) {
        String firma = sha256(referencia + valorEnCentavos + currency + integritySecret);
        String redirect = frontendUrl + "/pagar/resultado";
        return checkoutUrl
                + "?public-key=" + enc(publicKey)
                + "&currency=" + enc(currency)
                + "&amount-in-cents=" + valorEnCentavos
                + "&reference=" + enc(referencia)
                + "&signature:integrity=" + firma
                + "&redirect-url=" + enc(redirect);
    }

    private boolean firmaValida(JsonNode event) {
        JsonNode signature = event.path("signature");
        String checksum = signature.path("checksum").asText(null);
        JsonNode properties = signature.path("properties");
        String timestamp = event.path("timestamp").asText(null);

        if (checksum == null || !properties.isArray() || timestamp == null) {
            // Sin datos suficientes no es posible verificar; se permite solo si no
            // hay secreto configurado (entorno de desarrollo).
            return eventsSecret == null || eventsSecret.isBlank();
        }

        StringBuilder cadena = new StringBuilder();
        for (JsonNode prop : properties) {
            cadena.append(resolvePath(event.path("data"), prop.asText("")));
        }
        cadena.append(timestamp);

        String esperado = sha256(cadena + eventsSecret);
        return esperado.equalsIgnoreCase(checksum);
    }

    private String resolvePath(JsonNode data, String path) {
        JsonNode current = data;
        for (String part : path.split("\\.")) {
            if (current == null || current.isMissingNode()) {
                return "";
            }
            current = current.path(part);
        }
        return current.isMissingNode() || current.isNull() ? "" : current.asText("");
    }

    private String mapEstado(String status) {
        return switch (status == null ? "" : status.toUpperCase()) {
            case "APPROVED" -> "PAGADO";
            case "DECLINED", "VOIDED", "ERROR" -> "RECHAZADO";
            default -> "PENDIENTE";
        };
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo calcular la firma SHA-256", e);
        }
    }

    private String enc(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
