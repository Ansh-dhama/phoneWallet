package com.example.phoneWallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReversalRequest {

    @NotBlank(message = "Original transaction reference is required")
    private String originalTransactionReference;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}