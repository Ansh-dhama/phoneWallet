package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.WalletStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    boolean existsByUserIdAndCurrency(Long userId, String currency);
    Optional<Wallet> findByUserIdAndCurrency(Long userId, String currency);
    List<Wallet> findByUserIdOrderByCreatedAtAsc(Long userId);
    long countByStatus(WalletStatus status);
    Page<Wallet> findByStatus(WalletStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id in :ids order by w.id asc")
    List<Wallet> findAllByIdInOrderForUpdate(@Param("ids") Collection<Long> ids);
}
