package com.example.phoneWallet.dto;

import com.example.phoneWallet.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull Role role) {}
