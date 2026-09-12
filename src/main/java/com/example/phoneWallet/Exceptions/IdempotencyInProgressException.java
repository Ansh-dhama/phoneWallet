package com.example.phoneWallet.Exceptions;

public class IdempotencyInProgressException extends RuntimeException {
    public IdempotencyInProgressException(String message) { super(message); }
}
