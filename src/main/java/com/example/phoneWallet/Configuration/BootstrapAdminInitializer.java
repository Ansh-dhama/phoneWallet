package com.example.phoneWallet.Configuration;

import com.example.phoneWallet.Repository.UserRepository;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapAdminInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${wallet.bootstrap.admin.username:}") private String username;
    @Value("${wallet.bootstrap.admin.password:}") private String password;
    @Value("${wallet.bootstrap.admin.mobile:}") private String mobile;

    public BootstrapAdminInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (username == null || username.isBlank() || password == null || password.isBlank() || mobile == null || mobile.isBlank()) return;
        if (userRepository.existsByUsername(username.trim())) return;
        User admin = new User(username.trim(), mobile.trim(), passwordEncoder.encode(password), Role.ADMIN);
        userRepository.save(admin);
    }
}
