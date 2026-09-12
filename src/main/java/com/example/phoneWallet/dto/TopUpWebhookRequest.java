package com.example.phoneWallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record TopUpWebhookRequest(
        @NotBlank String providerOrderId,
        @NotBlank String providerPaymentId,
        @NotBlank @Pattern(regexp = "SUCCESS|FAILED") String status,
        @NotNull @DecimalMin(value = "1.00") BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency
) {}
