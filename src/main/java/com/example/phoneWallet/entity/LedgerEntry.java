package com.example.phoneWallet.entity;

import com.example.phoneWallet.enums.EntryType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hibernate will search for this variable name exactly!
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after_transaction", nullable = false)
    private BigDecimal balanceAfterTransaction;

    // Constructors, Getters

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Constructor for enforcement
    public LedgerEntry() {}

    public LedgerEntry(String transactionId, Long walletId, EntryType entryType, BigDecimal amount, BigDecimal balanceAfter) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfter;
    }

    // GETTERS ONLY (No Setters to enforce immutability)
    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public Long getWalletId() { return walletId; }
    public EntryType getEntryType() { return entryType; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfterTransaction() { return balanceAfterTransaction; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}