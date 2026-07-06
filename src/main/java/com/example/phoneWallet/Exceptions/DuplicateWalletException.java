package com.example.phoneWallet.Exceptions;

public class DuplicateWalletException extends RuntimeException {
    public DuplicateWalletException(Long userId, String currency) {
        super("User " + userId + " already has a wallet with currency " + currency);
    }
}