package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.TransactionBlock;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.entity.WalletBlocklist;
import com.example.phoneWallet.enums.WalletStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class RiskService {

    private static final BigDecimal MAX_TRANSACTION_LIMIT = new BigDecimal("50000");

    private final WalletService walletService;
    private final WalletBlocklistService walletBlocklistService;
    private final RedisFraudService refraudService;

    public RiskService(WalletService walletService, WalletBlocklistService walletBlocklistService, RedisFraudService refraudService) {
        this.walletService = walletService;
        this.walletBlocklistService = walletBlocklistService;
        this.refraudService = refraudService;
    }

    public void validateRisk(Long walletId, BigDecimal amount) {

        if (walletId == null) {
            throw new TransactionBlock("Wallet id is required");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new TransactionBlock("Amount must be greater than zero");
        }

        if (amount.compareTo(MAX_TRANSACTION_LIMIT) > 0) {
            throw new TransactionBlock("Transaction amount exceeds allowed limit");
        }

        Wallet wallet = walletService.findById(walletId);

        if (wallet.isFrozen()) {
            throw new TransactionBlock("Wallet is frozen");
        }
        if(walletBlocklistService.isBlacklisted(walletId)) {
            throw new TransactionBlock("Wallet  is blacklisted");
        }
        refraudService.checkTransactionVelocity(walletId);
    }
}