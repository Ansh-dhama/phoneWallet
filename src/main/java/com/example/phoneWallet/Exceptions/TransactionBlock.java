package com.example.phoneWallet.Exceptions;

public class TransactionBlock extends RuntimeException {
    public TransactionBlock(String message) {
        super(message);
    }
}
