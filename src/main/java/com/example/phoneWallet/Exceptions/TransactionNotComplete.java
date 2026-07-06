package com.example.phoneWallet.Exceptions;

public class TransactionNotComplete extends RuntimeException {
    public TransactionNotComplete(String message) {
        super(message);
    }
}
