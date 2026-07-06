package com.example.phoneWallet.dto;

import com.example.phoneWallet.enums.Role;
import lombok.Data;


@Data
public class SignUpRequest {
    private String username;
    private String password;
    private String mobile;
    private Role role;
}
