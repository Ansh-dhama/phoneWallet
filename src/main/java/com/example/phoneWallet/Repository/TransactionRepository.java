package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByTransactionReference(String transactionReference);

    List<Transaction> findByFromWalletIdOrToWalletId(Long fromWalletId, Long toWalletId);

    long countByStatus(TransactionStatus status);

    long countByType(TransactionType type);

    List<Transaction> findByStatus(TransactionStatus status);

    List<Transaction> findByType(TransactionType type);
}