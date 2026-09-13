package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.InvalidTopUpException;
import com.example.phoneWallet.dto.RazorpayPaymentVerificationRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class RazorpayPaymentService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String keyId;
    private final String keySecret;
    private final String webhookSecret;
    private final String apiBaseUrl;

    public RazorpayPaymentService(
            ObjectMapper objectMapper,
            @Value("${razorpay.key-id:}") String keyId,
            @Value("${razorpay.key-secret:}") String keySecret,
            @Value("${razorpay.webhook-secret:}") String webhookSecret,
            @Value("${razorpay.api-base-url:https://api.razorpay.com/v1}") String apiBaseUrl) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.keyId = trim(keyId);
        this.keySecret = trim(keySecret);
        this.webhookSecret = trim(webhookSecret);
        this.apiBaseUrl = trimTrailingSlash(apiBaseUrl);
    }

    public String publicKeyId() {
        ensureApiConfigured();
        return keyId;
    }

    public String createOrder(BigDecimal amount, String currency, Long walletId, Long userId) {
        ensureApiConfigured();
        validateCurrency(currency);

        long subunits = toSubunits(amount);
        Map<String, Object> body = Map.of(
                "amount", subunits,
                "currency", currency.toUpperCase(Locale.ROOT),
                "receipt", receipt(walletId),
                "notes", Map.of(
                        "walletId", String.valueOf(walletId),
                        "userId", String.valueOf(userId),
                        "purpose", "PHONEWALLET_TOPUP"));

        JsonNode response = sendJson("POST", "/orders", body);
        String orderId = text(response, "id");
        if (orderId == null || orderId.isBlank()) {
            throw new InvalidTopUpException("Razorpay did not return an order id");
        }
        return orderId;
    }

    public String verifyCapturedPayment(
            String expectedOrderId,
            BigDecimal expectedAmount,
            String expectedCurrency,
            RazorpayPaymentVerificationRequest request) {

        ensureApiConfigured();
        if (!expectedOrderId.equals(request.razorpayOrderId())) {
            throw new InvalidTopUpException("Razorpay order id does not match this top-up");
        }

        verifyCheckoutSignature(expectedOrderId, request.razorpayPaymentId(), request.razorpaySignature());
        return verifyCapturedPaymentById(
                expectedOrderId,
                expectedAmount,
                expectedCurrency,
                request.razorpayPaymentId());
    }

    public String verifyCapturedPaymentById(
            String expectedOrderId,
            BigDecimal expectedAmount,
            String expectedCurrency,
            String paymentId) {

        ensureApiConfigured();
        String encodedPaymentId = URLEncoder.encode(paymentId, StandardCharsets.UTF_8);
        JsonNode payment = sendJson("GET", "/payments/" + encodedPaymentId, null);

        String paymentOrderId = text(payment, "order_id");
        String paymentCurrency = text(payment, "currency");
        String paymentStatus = text(payment, "status");
        long actualSubunits = payment.path("amount").asLong(Long.MIN_VALUE);

        if (!expectedOrderId.equals(paymentOrderId)) {
            throw new InvalidTopUpException("Razorpay payment belongs to a different order");
        }
        if (!expectedCurrency.equalsIgnoreCase(paymentCurrency)) {
            throw new InvalidTopUpException("Razorpay payment currency does not match the top-up");
        }
        if (actualSubunits != toSubunits(expectedAmount)) {
            throw new InvalidTopUpException("Razorpay payment amount does not match the top-up");
        }
        if (!"captured".equalsIgnoreCase(paymentStatus)) {
            throw new InvalidTopUpException(
                    "Razorpay payment is not captured yet. Current status: " + paymentStatus);
        }
        return paymentId;
    }

    /**
     * Razorpay Checkout success is not trusted by itself. The signature must match
     * HMAC_SHA256(serverOrderId + "|" + paymentId, keySecret).
     */
    public void verifyCheckoutSignature(String orderId, String paymentId, String suppliedSignature) {
        ensureApiConfigured();
        String payload = orderId + "|" + paymentId;
        String expected = hmacSha256(payload, keySecret);
        if (!constantTimeEquals(expected, suppliedSignature)) {
            throw new InvalidTopUpException("Invalid Razorpay payment signature");
        }
    }

    /**
     * Verifies the exact raw body before parsing. This is required for Razorpay
     * webhook authentication.
     */
    public RazorpayWebhookPayment verifyAndParseWebhook(String rawBody, String suppliedSignature) {
        ensureWebhookConfigured();
        if (rawBody == null || rawBody.isBlank()) {
            throw new InvalidTopUpException("Empty Razorpay webhook payload");
        }
        if (suppliedSignature == null || suppliedSignature.isBlank()) {
            throw new InvalidTopUpException("Missing Razorpay webhook signature");
        }

        String expected = hmacSha256(rawBody, webhookSecret);
        if (!constantTimeEquals(expected, suppliedSignature)) {
            throw new InvalidTopUpException("Invalid Razorpay webhook signature");
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = text(root, "event");
            JsonNode payment = root.path("payload").path("payment").path("entity");
            if (payment.isMissingNode() || payment.isNull()) {
                return new RazorpayWebhookPayment(event, null, null, null, null, null, null);
            }

            Long amount = payment.hasNonNull("amount") ? payment.get("amount").asLong() : null;
            return new RazorpayWebhookPayment(
                    event,
                    text(payment, "id"),
                    text(payment, "order_id"),
                    amount,
                    text(payment, "currency"),
                    text(payment, "status"),
                    text(payment, "error_description"));
        } catch (IOException ex) {
            throw new InvalidTopUpException("Invalid Razorpay webhook payload");
        }
    }

    public long toSubunits(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidTopUpException("Top-up amount must be greater than zero");
        }
        try {
            return amount.setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2)
                    .longValueExact();
        } catch (ArithmeticException ex) {
            throw new InvalidTopUpException("Top-up amount must have at most 2 decimal places");
        }
    }

    private JsonNode sendJson(String method, String path, Object body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", basicAuth())
                    .header("Accept", "application/json");

            if ("POST".equals(method)) {
                String json = objectMapper.writeValueAsString(body);
                request.header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json));
            } else {
                request.GET();
            }

            HttpResponse<String> response = httpClient.send(
                    request.build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw providerHttpError(response.statusCode(), response.body());
            }
            return objectMapper.readTree(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InvalidTopUpException("Razorpay request was interrupted");
        } catch (IOException | IllegalArgumentException ex) {
            throw new InvalidTopUpException("Unable to communicate with Razorpay");
        }
    }

    private InvalidTopUpException providerHttpError(int status, String responseBody) {
        String description = null;
        try {
            JsonNode root = objectMapper.readTree(responseBody == null ? "{}" : responseBody);
            description = root.path("error").path("description").asText(null);
        } catch (Exception ignored) {
            // Keep provider error handling intentionally small and non-sensitive.
        }
        if (description == null || description.isBlank()) {
            description = "provider request failed";
        }
        return new InvalidTopUpException("Razorpay request failed (HTTP " + status + "): " + description);
    }

    private String basicAuth() {
        String raw = keyId + ":" + keySecret;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate Razorpay signature", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String supplied) {
        if (supplied == null) return false;
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                supplied.trim().getBytes(StandardCharsets.US_ASCII));
    }

    private void validateCurrency(String currency) {
        if (!"INR".equalsIgnoreCase(currency)) {
            throw new InvalidTopUpException("Razorpay top-up currently supports INR wallets only");
        }
    }

    private void ensureApiConfigured() {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new InvalidTopUpException(
                    "Razorpay is not configured. Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET");
        }
    }

    private void ensureWebhookConfigured() {
        if (webhookSecret.isBlank()) {
            throw new InvalidTopUpException(
                    "Razorpay webhook is not configured. Set RAZORPAY_WEBHOOK_SECRET");
        }
    }

    private String receipt(Long walletId) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "PW-" + walletId + "-" + random;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimTrailingSlash(String value) {
        String cleaned = trim(value);
        while (cleaned.endsWith("/")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }

    public record RazorpayWebhookPayment(
            String event,
            String paymentId,
            String orderId,
            Long amountSubunits,
            String currency,
            String status,
            String errorDescription) {
    }
}
