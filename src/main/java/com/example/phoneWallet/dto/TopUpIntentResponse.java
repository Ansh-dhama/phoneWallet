package com.example.phoneWallet.dto;

import com.example.phoneWallet.entity.TopUpIntent;
import com.example.phoneWallet.enums.TopUpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TopUpIntentResponse(
        Long id,
        Long walletId,
        BigDecimal amount,
        String currency,
        String providerOrderId,
        String providerPaymentId,
        String transactionReference,
        TopUpStatus status,
        boolean demoCompletionAllowed,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
    public static TopUpIntentResponse from(TopUpIntent intent, boolean demoCompletionAllowed) {
        return new TopUpIntentResponse(
                intent.getId(), intent.getWalletId(), intent.getAmount(), intent.getCurrency(),
                intent.getProviderOrderId(), intent.getProviderPaymentId(),
                intent.getTransactionReference(), intent.getStatus(), demoCompletionAllowed,
                intent.getCreatedAt(), intent.getCompletedAt()
        );
    }
}
