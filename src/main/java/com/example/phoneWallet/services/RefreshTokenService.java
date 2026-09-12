package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.RefreshTokenExpiredException;
import com.example.phoneWallet.Exceptions.RefreshTokenNotFoundException;
import com.example.phoneWallet.Exceptions.RefreshTokenRevokedException;
import com.example.phoneWallet.Repository.RefreshTokenRepository;
import com.example.phoneWallet.Repository.UserRepository;
import com.example.phoneWallet.Util.AuthUtil;
import com.example.phoneWallet.dto.RefreshTokenResponse;
import com.example.phoneWallet.entity.RefreshToken;
import com.example.phoneWallet.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, UserRepository userRepository, AuthUtil authUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authUtil = authUtil;
    }

    @Transactional
    public String createRefreshToken(User user) {
        String raw = newRawToken();
        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(hash(raw));
        entity.setUserId(user.getId());
        entity.setRevoked(false);
        entity.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(entity);
        return raw;
    }

    @Transactional
    public RefreshTokenResponse generateNewAccessToken(String rawToken) {
        RefreshToken old = refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));
        if (old.isRevoked()) throw new RefreshTokenRevokedException("Refresh token has been revoked");
        if (old.getExpiresAt().isBefore(LocalDateTime.now())) throw new RefreshTokenExpiredException("Refresh token has expired");

        User user = userRepository.findById(old.getUserId()).orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token user not found"));

        // Rotation: the token presented can never be used successfully again.
        old.setRevoked(true);
        refreshTokenRepository.save(old);
        String nextRaw = createRefreshToken(user);
        return new RefreshTokenResponse(authUtil.generateToken(user), nextRaw, user.getId());
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));
        if (!token.isRevoked()) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        }
    }

    private String newRawToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
