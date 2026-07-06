package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.UsernameExists;
import com.example.phoneWallet.Repository.UserRepository;
import com.example.phoneWallet.Util.AuthUtil;
import com.example.phoneWallet.dto.LoginRequest;
import com.example.phoneWallet.dto.LoginResponse;
import com.example.phoneWallet.dto.SignUpRequest;
import com.example.phoneWallet.dto.SignupResponse;
import com.example.phoneWallet.entity.RefreshToken;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.enums.AuditAction;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       AuthUtil authUtil,
                       PasswordEncoder passwordEncoder, AuditLogService auditLogService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.authUtil = authUtil;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.refreshTokenService = refreshTokenService;
    }

    public LoginResponse login(LoginRequest request) {

       try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = authUtil.generateToken(user);

           RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

           auditLogService.logSuccess(AuditAction.USER_LOGIN
            ,user.getId(),user.getUsername(),user.getRole(),null,null,"User Login Success");

           return new LoginResponse(token,refreshToken.getToken() , user.getId());


       }catch (Exception ex){
           auditLogService.logFailure(AuditAction.USER_LOGIN_FAILED
                   ,null,request.getUsername(),null,null,null,"User login failed",ex.getMessage());
           throw ex;
       }
    }

    public SignupResponse signUp(SignUpRequest request) {

       try {
            Optional<User> optionalUser = userRepository.findByUsername(request.getUsername());

            if (optionalUser.isPresent()) {
                throw new UsernameExists("Username already exists");
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setMobileNumber(request.getMobile());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(request.getRole());

            User savedUser = userRepository.save(user);
            auditLogService.logSuccess(AuditAction.USER_SIGNUP,user.getId(),user.getUsername(),user.getRole(),null,null,"User Sign Up Success");
            return new SignupResponse(savedUser.getId(), savedUser.getUsername());
        }catch (Exception ex){
           auditLogService.logFailure(
                   AuditAction.USER_SIGNUP_FAILED,null,request.getUsername(),null,null,null,"User Sign Up Failed",ex.getMessage()
           );
           throw ex;
       }
    }
}