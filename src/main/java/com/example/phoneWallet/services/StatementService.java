package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.LedgerRepository;
import com.example.phoneWallet.dto.StatementResponse;
import com.example.phoneWallet.entity.LedgerEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatementService {

    private final LedgerRepository ledgerRepository;

    public StatementService(LedgerRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    public List<StatementResponse> getStatementsByWalletId(Long walletId) {

        List<LedgerEntry> entries =
                ledgerRepository.findByWalletIdOrderByCreatedAtDesc(walletId);

        return entries.stream()
                .map(this::mapToStatementResponse)
                .collect(Collectors.toList());
    }

    public List<StatementResponse> getStatementByDateRange(
            Long walletId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        List<LedgerEntry> entries =
                ledgerRepository.findByWalletIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        walletId,
                        startDate,
                        endDate
                );

        return entries.stream()
                .map(this::mapToStatementResponse)
                .collect(Collectors.toList());
    }

    private StatementResponse mapToStatementResponse(LedgerEntry ledgerEntry) {

        StatementResponse statementResponse = new StatementResponse();

        statementResponse.setWalletId(ledgerEntry.getWalletId());
        statementResponse.setAmount(ledgerEntry.getAmount());
        statementResponse.setTimestamp(ledgerEntry.getCreatedAt());

        // Use this if your LedgerEntry has transactionReference
        statementResponse.setTransactionRef(ledgerEntry.getTransactionId());

        // If your entity has transactionId instead, then use:
        // statementResponse.setTransactionRef(ledgerEntry.getTransactionId());

        statementResponse.setEntryType(ledgerEntry.getEntryType());
        statementResponse.setBalanceAfterTransaction(
                ledgerEntry.getBalanceAfterTransaction()
        );

        return statementResponse;
    }
}