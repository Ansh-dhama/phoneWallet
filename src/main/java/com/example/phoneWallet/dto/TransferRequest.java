package com.example.phoneWallet.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull
    private Long fromWalletId;
    @NotNull private Long toWalletId;
    
    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;
    
    @NotBlank private String currency;
    @NotBlank private String idempotencyKey;
}