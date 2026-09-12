package com.example.phoneWallet.entity;

import com.example.phoneWallet.enums.EntryType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "ledger_entries",
        indexes = {
                @Index(name = "idx_ledger_tx", columnList = "transaction_id"),
                @Index(name = "idx_ledger_wallet_created", columnList = "wallet_id,created_at")
        }
)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, length = 40)
    private String transactionId;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 32)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after_transaction", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public LedgerEntry() {}

    public LedgerEntry(String transactionId, Long walletId, EntryType entryType, BigDecimal amount, BigDecimal balanceAfter) {
        this.transactionId = transactionId;
        this.walletId = walletId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfter;
    }

    public Long getId() { return id; }
    public String getTransactionId() { return transactionId; }
    public Long getWalletId() { return walletId; }
    public EntryType getEntryType() { return entryType; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfterTransaction() { return balanceAfterTransaction; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
