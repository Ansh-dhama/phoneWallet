package com.example.phoneWallet.Mapper;

import com.example.phoneWallet.dto.TransactionResponse;
import com.example.phoneWallet.entity.Transaction;

public class TransactionMapper {

    private TransactionMapper() {
        // utility class, object creation not allowed
    }

    public static TransactionResponse toResponse(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .fromWalletId(transaction.getFromWalletId())
                .toWalletId(transaction.getToWalletId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType() != null ? transaction.getType().name() : null)
                .status(transaction.getStatus() != null ? transaction.getStatus().name() : null)
                .failureReason(transaction.getFailureReason())
                .createdAt(transaction.getCreatedAt())
                .updatedAt(transaction.getUpdatedAt())
                .build();
    }
}