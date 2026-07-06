package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.*;
import com.example.phoneWallet.Repository.WalletRepository;
import com.example.phoneWallet.dto.WalletRequestDTO;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.WalletStatus;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class WalletService {

    private final WalletRepository walletRepository;
    private final AuditLogService auditLogService;

    public WalletService(WalletRepository walletRepository,
                         AuditLogService auditLogService) {
        this.walletRepository = walletRepository;
        this.auditLogService = auditLogService;
    }

    public Wallet createWallet(WalletRequestDTO dto) {

        try {
            boolean exists = walletRepository.existsByUserIdAndCurrency(
                    dto.getUserId(),
                    dto.getCurrency()
            );

            if (exists) {
                throw new DuplicateWalletException(dto.getUserId(), dto.getCurrency());
            }

            Wallet wallet = new Wallet();
            wallet.setWalletNumber(UUID.randomUUID().toString());
            wallet.setUserId(dto.getUserId());
            wallet.setCurrency(dto.getCurrency());
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setStatus(WalletStatus.ACTIVE);

            Wallet savedWallet = walletRepository.save(wallet);

            auditLogService.logSuccess(
                    AuditAction.WALLET_CREATED,
                    dto.getUserId(),
                    null,
                    null,
                    savedWallet.getId(),
                    null,
                    "Wallet created successfully for currency " + dto.getCurrency()
            );

            return savedWallet;

        } catch (Exception ex) {

            auditLogService.logFailure(
                    AuditAction.WALLET_CREATED,
                    dto.getUserId(),
                    null,
                    null,
                    null,
                    null,
                    "Wallet creation failed for currency " + dto.getCurrency(),
                    ex.getMessage()
            );

            throw ex;
        }
    }

    public BigDecimal getBalance(Long walletId) {

        try {
            Wallet wallet = findById(walletId);

            if (wallet.getStatus() == WalletStatus.FROZEN) {
                throw new WalletFrozenException(
                        "Wallet has been frozen. Current account balance is " + wallet.getBalance()
                );
            }

            auditLogService.logSuccess(
                    AuditAction.WALLET_BALANCE_VIEWED,
                    wallet.getUserId(),
                    null,
                    null,
                    wallet.getId(),
                    null,
                    "Wallet balance viewed successfully"
            );

            return wallet.getBalance();

        } catch (Exception ex) {

            auditLogService.logFailure(
                    AuditAction.WALLET_BALANCE_VIEWED,
                    null,
                    null,
                    null,
                    walletId,
                    null,
                    "Wallet balance view failed",
                    ex.getMessage()
            );

            throw ex;
        }
    }

    public void freezeWallet(Long walletId) {

        try {
            Wallet wallet = findByIdForUpdate(walletId);
            wallet.setStatus(WalletStatus.FROZEN);
            walletRepository.save(wallet);

            auditLogService.logSuccess(
                    AuditAction.WALLET_FROZEN,
                    wallet.getUserId(),
                    null,
                    null,
                    wallet.getId(),
                    null,
                    "Wallet frozen successfully"
            );

        } catch (Exception ex) {

            auditLogService.logFailure(
                    AuditAction.WALLET_FROZEN,
                    null,
                    null,
                    null,
                    walletId,
                    null,
                    "Wallet freeze failed",
                    ex.getMessage()
            );

            throw ex;
        }
    }

    public void unfreezeWallet(Long walletId) {

        try {
            Wallet wallet = findByIdForUpdate(walletId);
            wallet.setStatus(WalletStatus.ACTIVE);
            walletRepository.save(wallet);

            auditLogService.logSuccess(
                    AuditAction.WALLET_UNFROZEN,
                    wallet.getUserId(),
                    null,
                    null,
                    wallet.getId(),
                    null,
                    "Wallet unfrozen successfully"
            );

        } catch (Exception ex) {

            auditLogService.logFailure(
                    AuditAction.WALLET_UNFROZEN,
                    null,
                    null,
                    null,
                    walletId,
                    null,
                    "Wallet unfreeze failed",
                    ex.getMessage()
            );

            throw ex;
        }
    }

    public Wallet findById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    public Wallet findByIdForUpdate(Long id) {
        return walletRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }

    public Wallet credit(Long walletId, BigDecimal amount) {
        Wallet wallet = findByIdForUpdate(walletId);

        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Cannot credit: Wallet is frozen.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AmountNegativeException("Credit amount must be greater than zero. Provided: " + amount);
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    public Wallet debit(Long walletId, BigDecimal amount) {
        Wallet wallet = findByIdForUpdate(walletId);

        if (wallet.getStatus() == WalletStatus.FROZEN) {
            throw new WalletFrozenException("Cannot debit: Wallet is frozen.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AmountNegativeException("Debit amount must be greater than zero. Provided: " + amount);
        }

        if (amount.compareTo(wallet.getBalance()) > 0) {
            throw new InsufficentAmountException("Insufficient funds. Wallet balance is " + wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        return walletRepository.save(wallet);
    }

    public void save(Wallet wallet) {
        walletRepository.save(wallet);
    }
}