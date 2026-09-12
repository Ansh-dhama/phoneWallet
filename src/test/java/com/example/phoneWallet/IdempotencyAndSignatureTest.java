package com.example.phoneWallet;

import com.example.phoneWallet.Exceptions.InvalidTopUpException;
import com.example.phoneWallet.Util.IdempotencyKeyUtil;
import com.example.phoneWallet.dto.TopUpWebhookRequest;
import com.example.phoneWallet.services.TopUpSignatureService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IdempotencyAndSignatureTest {

    @Test
    void idempotencyKeysAreStablePerActorAndDifferentAcrossActors() {
        String a1 = IdempotencyKeyUtil.scoped(1L, "same-client-key");
        String a2 = IdempotencyKeyUtil.scoped(1L, "same-client-key");
        String b = IdempotencyKeyUtil.scoped(2L, "same-client-key");
        assertEquals(a1, a2);
        assertNotEquals(a1, b);
        assertEquals(64, a1.length());
    }

    @Test
    void topUpWebhookRejectsTampering() {
        TopUpSignatureService service = new TopUpSignatureService("12345678901234567890123456789012");
        TopUpWebhookRequest request = new TopUpWebhookRequest(
                "TOPUP-1", "PAY-1", "SUCCESS", new BigDecimal("500.00"), "INR"
        );
        String signature = service.sign(request);
        assertDoesNotThrow(() -> service.verify(request, signature));
        assertThrows(InvalidTopUpException.class, () -> service.verify(
                new TopUpWebhookRequest("TOPUP-1", "PAY-1", "SUCCESS", new BigDecimal("501.00"), "INR"),
                signature
        ));
    }
}
