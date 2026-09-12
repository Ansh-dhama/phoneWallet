package com.example.phoneWallet.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long transactionId;
    private String transactionReference;
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private BigDecimal refundedAmount;
    private String currency;
    private String type;
    private String status;
    private String relatedTransactionReference;
    private String merchantReference;
    private String providerReference;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
