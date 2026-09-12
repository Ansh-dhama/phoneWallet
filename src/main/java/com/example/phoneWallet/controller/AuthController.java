package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.services.AuthService;
import com.example.phoneWallet.services.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/refresh-token")
    public RefreshTokenResponse refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return refreshTokenService.generateNewAccessToken(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public String logout(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return "Logout successful";
    }
}