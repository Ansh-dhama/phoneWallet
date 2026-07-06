package com.example.phoneWallet.dto;

import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionEvent {

    private String transactionReference;
    private Long fromWalletId;
    private Long toWalletId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private String message;
}