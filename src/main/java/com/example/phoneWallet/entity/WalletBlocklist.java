package com.example.phoneWallet.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "wallet_blacklist",
        uniqueConstraints = @UniqueConstraint(name = "uk_blacklist_wallet", columnNames = {"wallet_id"}),
        indexes = @Index(name = "idx_blacklist_active", columnList = "active"))
public class WalletBlocklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="wallet_id", nullable = false, unique = true)
    private Long walletId;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_blacklist_wallet"))
    private Wallet wallet;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "blacklisted_by")
    private String blacklistedBy;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}