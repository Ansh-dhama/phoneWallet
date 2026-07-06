package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.WalletStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    boolean existsByUserIdAndCurrency(Long userId, String currency);

    Optional<Wallet> findByUserIdAndCurrency(Long userId, String currency);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findById(Long id);

    long countByStatus(WalletStatus status);

    List<Wallet> findByStatus(WalletStatus status);

    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(Long id);
}