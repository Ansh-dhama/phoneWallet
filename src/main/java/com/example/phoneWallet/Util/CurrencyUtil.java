package com.example.phoneWallet.Util;

import com.example.phoneWallet.Exceptions.InvalidTransactionException;

import java.util.Currency;
import java.util.Locale;

public final class CurrencyUtil {
    private CurrencyUtil() {}

    public static String normalize(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new InvalidTransactionException("Currency is required");
        }
        String code = currency.trim().toUpperCase(Locale.ROOT);
        try {
            Currency.getInstance(code);
            return code;
        } catch (IllegalArgumentException ex) {
            throw new InvalidTransactionException("Unsupported ISO-4217 currency: " + code);
        }
    }
}
