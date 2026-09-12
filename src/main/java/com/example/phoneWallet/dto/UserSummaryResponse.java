package com.example.phoneWallet.dto;

import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.enums.Role;

public record UserSummaryResponse(Long id, String username, String mobile, Role role) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername(), user.getMobileNumber(), user.getRole());
    }
}
