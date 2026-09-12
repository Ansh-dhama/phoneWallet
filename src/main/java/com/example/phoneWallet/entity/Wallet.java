package com.example.phoneWallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.phoneWallet.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wallet_user_currency", columnNames = {"user_id", "currency"}),
                @UniqueConstraint(name = "uk_wallet_number", columnNames = "wallet_number")
        },
        indexes = {
                @Index(name = "idx_wallet_user", columnList = "user_id"),
                @Index(name = "idx_wallet_status", columnList = "status")
        }
)
@Getter
@Setter
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version = 0L;

    @Column(name = "wallet_number", nullable = false, length = 64)
    private String walletNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_wallet_user"))
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status;

    // Keeps a manual admin freeze independent from blacklist state.
    @Column(name = "administratively_frozen", nullable = false)
    private boolean administrativelyFrozen = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (balance == null) balance = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isFrozen() {
        return status == WalletStatus.FROZEN;
    }
}
