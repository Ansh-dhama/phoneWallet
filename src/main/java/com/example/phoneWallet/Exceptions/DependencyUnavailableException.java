package com.example.phoneWallet.Exceptions;

public class DependencyUnavailableException extends RuntimeException {
    public DependencyUnavailableException(String message) { super(message); }
    public DependencyUnavailableException(String message, Throwable cause) { super(message, cause); }
}
