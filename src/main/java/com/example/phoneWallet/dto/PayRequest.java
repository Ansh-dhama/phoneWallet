package com.example.phoneWallet.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class PayRequest {
    @NotNull(message = "Customer wallet ID is required")
    private Long fromWalletId;

    @NotNull(message = "Merchant wallet ID is required")
    private Long toWalletId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be a 3-letter ISO code")
    private String currency;

    @NotBlank(message = "Idempotency key is required")
    @Size(max = 128)
    private String idempotencyKey;

    @Size(max = 128)
    private String merchantReference;
}
