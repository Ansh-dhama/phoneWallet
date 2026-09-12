package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.UsernameExists;
import com.example.phoneWallet.Repository.UserRepository;
import com.example.phoneWallet.Util.AuthUtil;
import com.example.phoneWallet.dto.LoginRequest;
import com.example.phoneWallet.dto.LoginResponse;
import com.example.phoneWallet.dto.SignUpRequest;
import com.example.phoneWallet.dto.SignupResponse;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.Role;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, AuthUtil authUtil,
                       PasswordEncoder passwordEncoder, AuditLogService auditLogService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.authUtil = authUtil;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            User user = userRepository.findByUsername(username).orElseThrow();
            String accessToken = authUtil.generateToken(user);
            String refreshToken = refreshTokenService.createRefreshToken(user);
            auditLogService.logSuccess(AuditAction.USER_LOGIN, user.getId(), user.getUsername(), user.getRole(), null, null, "User login successful");
            return new LoginResponse(accessToken, refreshToken, user.getId());
        } catch (Exception ex) {
            auditLogService.logFailure(AuditAction.USER_LOGIN_FAILED, null, username, null, null, null, "User login failed", ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public SignupResponse signUp(SignUpRequest request) {
        String username = request.getUsername().trim();
        String mobile = request.getMobile().trim();
        try {
            if (userRepository.existsByUsername(username)) throw new UsernameExists("Username already exists");
            if (userRepository.existsByMobileNumber(mobile)) throw new UsernameExists("Mobile number is already registered");

            User user = new User();
            user.setUsername(username);
            user.setMobileNumber(mobile);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            // Public signup can NEVER choose ADMIN or MERCHANT.
            user.setRole(Role.USER);

            User saved = userRepository.save(user);
            auditLogService.logSuccess(AuditAction.USER_SIGNUP, saved.getId(), saved.getUsername(), saved.getRole(), null, null, "User signup successful");
            return new SignupResponse(saved.getId(), saved.getUsername());
        } catch (Exception ex) {
            auditLogService.logFailure(AuditAction.USER_SIGNUP_FAILED, null, username, null, null, null, "User signup failed", ex.getMessage());
            throw ex;
        }
    }
}
