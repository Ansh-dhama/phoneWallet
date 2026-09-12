package com.example.phoneWallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.phoneWallet.enums.TopUpStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name = "topup_intents",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_topup_provider_order", columnNames = "provider_order_id"),
                @UniqueConstraint(name = "uk_topup_idempotency", columnNames = "idempotency_key")
        },
        indexes = {
                @Index(name = "idx_topup_wallet_created", columnList = "wallet_id,created_at"),
                @Index(name = "idx_topup_status", columnList = "status")
        }
)
public class TopUpIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version = 0L;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_topup_wallet"))
    private Wallet wallet;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_topup_user"))
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "provider_order_id", nullable = false, length = 80)
    private String providerOrderId;

    @Column(name = "provider_payment_id", length = 128)
    private String providerPaymentId;

    @Column(name = "transaction_reference", length = 40)
    private String transactionReference;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TopUpStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null) status = TopUpStatus.PENDING;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
