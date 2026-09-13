package com.example.phoneWallet;

import com.example.phoneWallet.Exceptions.InvalidTopUpException;
import com.example.phoneWallet.dto.RazorpayPaymentVerificationRequest;
import com.example.phoneWallet.services.RazorpayPaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class RazorpayPaymentServiceTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }

    @Test
    void checkoutSignatureAcceptsValidSignatureAndRejectsTampering() {
        RazorpayPaymentService service = service("http://localhost:1/v1");
        String signature = hmac("order_1|pay_1", "test_secret");

        assertDoesNotThrow(() -> service.verifyCheckoutSignature("order_1", "pay_1", signature));
        assertThrows(InvalidTopUpException.class,
                () -> service.verifyCheckoutSignature("order_1", "pay_2", signature));
    }

    @Test
    void webhookSignatureUsesExactRawBody() {
        RazorpayPaymentService service = service("http://localhost:1/v1");
        String raw = "{\"event\":\"payment.captured\",\"payload\":{\"payment\":{\"entity\":{\"id\":\"pay_1\",\"order_id\":\"order_1\",\"amount\":50000,\"currency\":\"INR\",\"status\":\"captured\"}}}}";
        String signature = hmac(raw, "webhook_secret");

        RazorpayPaymentService.RazorpayWebhookPayment event =
                service.verifyAndParseWebhook(raw, signature);

        assertEquals("payment.captured", event.event());
        assertEquals("pay_1", event.paymentId());
        assertEquals("order_1", event.orderId());
        assertEquals(50000L, event.amountSubunits());
        assertThrows(InvalidTopUpException.class,
                () -> service.verifyAndParseWebhook(raw + " ", signature));
    }

    @Test
    void createsOrderAndVerifiesCapturedPaymentAgainstProviderResponses() throws Exception {
        AtomicReference<String> orderRequestBody = new AtomicReference<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);

        server.createContext("/v1/orders", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            orderRequestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            respond(exchange, 200, "{\"id\":\"order_1\",\"amount\":50000,\"currency\":\"INR\",\"status\":\"created\"}");
        });

        server.createContext("/v1/payments/pay_1", exchange -> {
            assertEquals("GET", exchange.getRequestMethod());
            respond(exchange, 200, "{\"id\":\"pay_1\",\"order_id\":\"order_1\",\"amount\":50000,\"currency\":\"INR\",\"status\":\"captured\"}");
        });
        server.start();

        String baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
        RazorpayPaymentService service = service(baseUrl);

        String orderId = service.createOrder(new BigDecimal("500.00"), "INR", 7L, 9L);
        assertEquals("order_1", orderId);
        assertTrue(orderRequestBody.get().contains("\"amount\":50000"));
        assertTrue(orderRequestBody.get().contains("\"currency\":\"INR\""));

        String signature = hmac("order_1|pay_1", "test_secret");
        String paymentId = service.verifyCapturedPayment(
                "order_1",
                new BigDecimal("500.00"),
                "INR",
                new RazorpayPaymentVerificationRequest("pay_1", "order_1", signature));

        assertEquals("pay_1", paymentId);
    }

    @Test
    void refusesUncapturedPayment() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/payments/pay_2", exchange ->
                respond(exchange, 200, "{\"id\":\"pay_2\",\"order_id\":\"order_2\",\"amount\":10000,\"currency\":\"INR\",\"status\":\"authorized\"}"));
        server.start();

        RazorpayPaymentService service = service(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");

        assertThrows(InvalidTopUpException.class, () ->
                service.verifyCapturedPaymentById(
                        "order_2", new BigDecimal("100.00"), "INR", "pay_2"));
    }

    private RazorpayPaymentService service(String baseUrl) {
        return new RazorpayPaymentService(
                new ObjectMapper(),
                "rzp_test_key",
                "test_secret",
                "webhook_secret",
                baseUrl);
    }

    private static String hmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
