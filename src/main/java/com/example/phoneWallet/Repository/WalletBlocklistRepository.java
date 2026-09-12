package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.WalletBlocklist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletBlocklistRepository extends JpaRepository<WalletBlocklist, Long> {
    Optional<WalletBlocklist> findByWalletId(Long walletId);
    Page<WalletBlocklist> findByActiveTrue(Pageable pageable);
    long countByActiveTrue();
    Optional<WalletBlocklist> findByWalletIdAndActiveTrue(Long walletId);
    boolean existsByWalletIdAndActiveTrue(Long walletId);
}
