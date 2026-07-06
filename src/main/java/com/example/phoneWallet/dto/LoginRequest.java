package com.example.phoneWallet.dto;


import com.example.phoneWallet.enums.Role;
import lombok.Data;

@Data
public class LoginRequest {


    private String username;
    private String password;

}
