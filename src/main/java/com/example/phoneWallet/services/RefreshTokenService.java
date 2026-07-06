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

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final AuthUtil authUtil;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               AuthUtil authUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.authUtil = authUtil;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUserId(user.getId());
        refreshToken.setRevoked(false);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshTokenResponse generateNewAccessToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RefreshTokenRevokedException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RefreshTokenExpiredException("Refresh token has expired");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = authUtil.generateToken(user);

        return new RefreshTokenResponse(
                newAccessToken,
                refreshToken.getToken(),
                user.getId()
        );
    }

    @Transactional
    public void revokeRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}