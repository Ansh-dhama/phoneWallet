package com.example.phoneWallet.dto;

import com.example.phoneWallet.enums.EntryType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StatementResponse {
    private String transactionRef;
    private Long walletId;
    private EntryType entryType;
    private BigDecimal amount;
    private BigDecimal balanceAfterTransaction;
    private LocalDateTime timestamp;
}
