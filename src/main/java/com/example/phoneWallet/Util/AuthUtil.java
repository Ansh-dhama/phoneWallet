package com.example.phoneWallet.Util;

import com.example.phoneWallet.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;

@Component
public class AuthUtil {
    private final String secretKey;
    private final Duration accessTokenTtl;

    public AuthUtil(@Value("${jwt.secretKey}") String secretKey,
                    @Value("${jwt.access-token-minutes:10}") long accessTokenMinutes) {
        this.secretKey = secretKey;
        this.accessTokenTtl = Duration.ofMinutes(accessTokenMinutes);
    }

    @PostConstruct
    void validateSecret() {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + accessTokenTtl.toMillis()))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractUsername(String token) { return extractClaims(token).getSubject(); }

    public boolean isTokenValid(String token) {
        return extractClaims(token).getExpiration().after(new Date());
    }
}
