package com.example.phoneWallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TopUpIntentRequest(
        @NotNull @DecimalMin(value = "1.00") BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
        @NotBlank @Size(max = 128) String idempotencyKey
) {}
