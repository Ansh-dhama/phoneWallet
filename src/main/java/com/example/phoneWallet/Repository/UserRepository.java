package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByMobileNumber(String mobileNumber);
    Page<User> findByRole(Role role, Pageable pageable);
}
