package com.example.phoneWallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequest {
    @NotNull(message = "transactionRefernce must be required")
    private String originalTransactionReference;
    @NotNull
    private BigDecimal refundAmount;
    private String refundReason;
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
@NotNull
  private String currency;
}
