package com.example.phoneWallet.Exceptions;

public class LedgerValidationException extends RuntimeException
{
    public LedgerValidationException(String message) {
        super(message);
    }
}
