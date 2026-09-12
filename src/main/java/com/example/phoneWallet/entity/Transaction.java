package com.example.phoneWallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tx_idempotency", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uk_tx_reference", columnNames = "transaction_reference")
        },
        indexes = {
                @Index(name = "idx_tx_from_wallet_created", columnList = "from_wallet_id,created_at"),
                @Index(name = "idx_tx_to_wallet_created", columnList = "to_wallet_id,created_at"),
                @Index(name = "idx_tx_status_created", columnList = "status,created_at"),
                @Index(name = "idx_tx_type_created", columnList = "type,created_at"),
                @Index(name = "idx_tx_related_reference", columnList = "related_transaction_reference")
        }
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version = 0L;

    @Column(name = "transaction_reference", nullable = false, length = 40)
    private String transactionReference;

    @Column(name = "from_wallet_id")
    private Long fromWalletId;

    @Column(name = "to_wallet_id")
    private Long toWalletId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_wallet_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_tx_from_wallet"))
    private Wallet fromWallet;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_wallet_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_tx_to_wallet"))
    private Wallet toWallet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TransactionStatus status;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "refunded_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "related_transaction_reference", length = 40)
    private String relatedTransactionReference;

    @Column(name = "merchant_reference", length = 128)
    private String merchantReference;

    @Column(name = "provider_reference", length = 128)
    private String providerReference;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (refundedAmount == null) refundedAmount = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
