package com.example.phoneWallet.enums;

public enum EntryType {
    DEBIT,
    CREDIT,
    REVERSAL_DEBIT,
    REVERSAL_CREDIT,
    REFUND_DEBIT,
    REFUND_CREDIT,

    // External settlement/clearing side used for wallet top-ups.
    EXTERNAL_DEBIT,
    EXTERNAL_CREDIT
}
