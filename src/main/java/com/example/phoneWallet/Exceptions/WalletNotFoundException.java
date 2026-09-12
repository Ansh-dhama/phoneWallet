package com.example.phoneWallet.Exceptions;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(Long id) {
        super("Wallet not found with ID: " + id);
    }
}
