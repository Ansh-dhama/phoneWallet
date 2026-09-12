package com.example.phoneWallet.Exceptions;

public class InsufficentAmountException extends RuntimeException {
    public InsufficentAmountException(String message) {
        super(message);
    }
}
