package com.example.phoneWallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReversalRequest {
    @NotBlank(message = "Original transaction reference is required")
    private String originalTransactionReference;

    @NotBlank(message = "Reason is required")
    @Size(max = 500)
    private String reason;

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 128)
    private String idempotencyKey;
}
