package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface LedgerRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByTransactionId(String transactionId);
    List<LedgerEntry> findByWalletId(Long walletId);

    @Query(value = """
            SELECT COALESCE(SUM(amount), 0)
            FROM ledger_entries
            WHERE transaction_id = :txRef
            AND entry_type IN (:types)
            """, nativeQuery = true)
    BigDecimal sumAmountByTransactionIdAndTypes(
            @Param("txRef") String txRef,
            @Param("types") List<String> types
    );

    Page<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    Page<LedgerEntry> findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long walletId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable
    );
}
