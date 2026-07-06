package com.example.phoneWallet.Exceptions;

public class WalletFrozenException extends RuntimeException {
    public WalletFrozenException(String message) {
        super(message);
    }
}