package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.InvalidTopUpException;
import com.example.phoneWallet.dto.TopUpWebhookRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class TopUpSignatureService {
    private final String webhookSecret;

    public TopUpSignatureService(@Value("${wallet.topup.webhook-secret}") String webhookSecret) {
        if (webhookSecret == null || webhookSecret.length() < 24) {
            throw new IllegalStateException("wallet.topup.webhook-secret must be configured with at least 24 characters");
        }
        this.webhookSecret = webhookSecret;
    }

    public void verify(TopUpWebhookRequest request, String suppliedSignature) {
        if (suppliedSignature == null || suppliedSignature.isBlank()) {
            throw new InvalidTopUpException("Missing top-up webhook signature");
        }
        String expected = sign(request);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII), suppliedSignature.trim().getBytes(StandardCharsets.US_ASCII))) {
            throw new InvalidTopUpException("Invalid top-up webhook signature");
        }
    }

    public String sign(TopUpWebhookRequest request) {
        try {
            String canonical = request.providerOrderId() + "|" + request.providerPaymentId() + "|"
                    + request.status().toUpperCase(Locale.ROOT) + "|" + money(request.amount()) + "|"
                    + request.currency().toUpperCase(Locale.ROOT);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to calculate webhook signature", ex);
        }
    }

    private String money(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
