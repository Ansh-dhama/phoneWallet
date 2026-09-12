package com.example.phoneWallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Kept for source compatibility. Public clients should use /api/topups instead.
 */
@Getter
@Setter
@Deprecated
public class LoadMoneyRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Za-z]{3}$")
    private String currency;

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 128)
    private String idempotencyKey;

    @Size(max = 500)
    private String remarks;
}
