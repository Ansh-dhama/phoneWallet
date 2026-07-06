package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.LedgerRepository;
import com.example.phoneWallet.dto.StatementResponse;
import com.example.phoneWallet.entity.LedgerEntry;
import com.example.phoneWallet.enums.EntryType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;

    public LedgerService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public void recordEntry(
            String transactionId,
            Long walletId,
            EntryType entryType,
            BigDecimal amount,
            BigDecimal balanceAfterTransaction
    ) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction id is required");
        }

        if (walletId == null) {
            throw new IllegalArgumentException("Wallet id is required");
        }

        if (entryType == null) {
            throw new IllegalArgumentException("Entry type is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ledger amount must be greater than zero");
        }

        if (balanceAfterTransaction == null) {
            throw new IllegalArgumentException("Balance after transaction is required");
        }

        LedgerEntry entry = new LedgerEntry(
                transactionId,
                walletId,
                entryType,
                amount,
                balanceAfterTransaction
        );

        ledgerRepository.save(entry);
    }

    public List<LedgerEntry> getLedgerByTransactionId(String transactionId) {
        return ledgerRepository.findByTransactionId(transactionId);
    }


    public List<LedgerEntry> getLedgerByWalletId(Long walletId) {
        return ledgerRepository.findByWalletId(walletId);
    }
}