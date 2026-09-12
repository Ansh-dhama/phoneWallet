package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Optional<Transaction> findByTransactionReference(String transactionReference);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transaction t where t.transactionReference = :reference")
    Optional<Transaction> findByTransactionReferenceForUpdate(@Param("reference") String reference);

    Page<Transaction> findByFromWalletIdOrToWalletId(Long fromWalletId, Long toWalletId, Pageable pageable);
    Page<Transaction> findByRelatedTransactionReference(String relatedTransactionReference, Pageable pageable);

    long countByStatus(TransactionStatus status);
    long countByType(TransactionType type);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
    Page<Transaction> findByType(TransactionType type, Pageable pageable);
}
