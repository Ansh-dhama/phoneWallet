package com.example.phoneWallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlacklistWalletRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    private String blacklistedBy;
}