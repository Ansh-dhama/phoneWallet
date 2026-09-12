package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.TopUpIntent;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TopUpIntentRepository extends JpaRepository<TopUpIntent, Long> {
    Optional<TopUpIntent> findByIdempotencyKey(String idempotencyKey);
    Optional<TopUpIntent> findByProviderOrderId(String providerOrderId);
    Page<TopUpIntent> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TopUpIntent t where t.id = :id")
    Optional<TopUpIntent> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TopUpIntent t where t.providerOrderId = :providerOrderId")
    Optional<TopUpIntent> findByProviderOrderIdForUpdate(@Param("providerOrderId") String providerOrderId);
}
