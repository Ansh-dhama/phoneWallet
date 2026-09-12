package com.example.phoneWallet.Exceptions;

public class UsernameExists extends RuntimeException {
    public UsernameExists(String message) {
        super(message);
    }
}
