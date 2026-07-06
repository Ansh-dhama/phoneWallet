package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.WalletBlocklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WalletBlocklistRepository extends JpaRepository<WalletBlocklist, Long> {

    Optional<WalletBlocklist> findByWalletId(Long walletId);

    boolean existsByWalletId(Long walletId);

    List<WalletBlocklist> findByActiveTrue();

    long countByActiveTrue();

    Optional<WalletBlocklist> findByWalletIdAndActiveTrue(Long walletId);

    boolean existsByWalletIdAndActiveTrue(Long walletId);
}