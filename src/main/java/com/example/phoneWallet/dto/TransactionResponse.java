package com.example.phoneWallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String currency;
    private String type;
    private String status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}