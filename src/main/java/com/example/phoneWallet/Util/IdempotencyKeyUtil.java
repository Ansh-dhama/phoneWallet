package com.example.phoneWallet.Util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Stores client idempotency keys as an actor-scoped SHA-256 digest.
 * This prevents two different users from colliding on the same client key and
 * avoids persisting the raw client token in the transaction tables.
 */
public final class IdempotencyKeyUtil {
    private IdempotencyKeyUtil() {}

    public static String scoped(Long actorId, String rawKey) {
        if (actorId == null) throw new IllegalArgumentException("Authenticated actor is required for idempotency");
        if (rawKey == null || rawKey.isBlank()) throw new IllegalArgumentException("Idempotency key is required");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((actorId + ":" + rawKey.trim()).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
