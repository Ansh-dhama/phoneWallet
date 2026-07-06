package com.example.phoneWallet.Exceptions;

public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException(String message) {
        super(message);
    }
}