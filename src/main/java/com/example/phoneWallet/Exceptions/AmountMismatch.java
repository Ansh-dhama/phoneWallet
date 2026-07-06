package com.example.phoneWallet.Exceptions;

public class AmountMismatch extends RuntimeException {
    public AmountMismatch(String message) {
        super(message);
    }
}
