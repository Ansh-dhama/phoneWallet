package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.*;
import com.example.phoneWallet.Repository.WalletBlocklistRepository;
import com.example.phoneWallet.Repository.WalletRepository;
import com.example.phoneWallet.Util.CurrencyUtil;
import com.example.phoneWallet.dto.WalletRequestDTO;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.WalletStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletBlocklistRepository blocklistRepository;
    private final AuditLogService auditLogService;
    private final CurrentUserService currentUserService;

    public WalletService(WalletRepository walletRepository, WalletBlocklistRepository blocklistRepository,
                         AuditLogService auditLogService, CurrentUserService currentUserService) {
        this.walletRepository = walletRepository;
        this.blocklistRepository = blocklistRepository;
        this.auditLogService = auditLogService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public Wallet createWallet(WalletRequestDTO dto) {
        User user = currentUserService.requireCurrentUser();
        String currency = CurrencyUtil.normalize(dto.getCurrency());
        try {
            if (walletRepository.existsByUserIdAndCurrency(user.getId(), currency)) {
                throw new DuplicateWalletException(user.getId(), currency);
            }
            Wallet wallet = new Wallet();
            wallet.setWalletNumber(UUID.randomUUID().toString());
            wallet.setUserId(user.getId());
            wallet.setCurrency(currency);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setStatus(WalletStatus.ACTIVE);
            wallet.setAdministrativelyFrozen(false);
            Wallet saved = walletRepository.save(wallet);
            auditLogService.logSuccess(AuditAction.WALLET_CREATED, user.getId(), user.getUsername(), user.getRole(), saved.getId(), null,
                    "Wallet created successfully for currency " + currency);
            return saved;
        } catch (Exception ex) {
            auditLogService.logFailure(AuditAction.WALLET_CREATED, user.getId(), user.getUsername(), user.getRole(), null, null,
                    "Wallet creation failed for currency " + currency, ex.getMessage());
            throw ex;
        }
    }

    public List<Wallet> getMyWallets() {
        User user = currentUserService.requireCurrentUser();
        return walletRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
    }

    public BigDecimal getBalance(Long walletId) {
        // Freezing prevents money movement; it should not hide the owner's balance.
        return findById(walletId).getBalance();
    }

    @Transactional
    public void freezeWallet(Long walletId) {
        Wallet wallet = findByIdForUpdate(walletId);
        wallet.setAdministrativelyFrozen(true);
        wallet.setStatus(WalletStatus.FROZEN);
        walletRepository.save(wallet);
        auditLogService.logSuccess(AuditAction.WALLET_FROZEN, wallet.getUserId(), null, null, wallet.getId(), null, "Wallet frozen successfully");
    }

    @Transactional
    public void unfreezeWallet(Long walletId) {
        Wallet wallet = findByIdForUpdate(walletId);
        wallet.setAdministrativelyFrozen(false);
        wallet.setStatus(blocklistRepository.existsByWalletIdAndActiveTrue(walletId) ? WalletStatus.FROZEN : WalletStatus.ACTIVE);
        walletRepository.save(wallet);
        auditLogService.logSuccess(AuditAction.WALLET_UNFROZEN, wallet.getUserId(), null, null, wallet.getId(), null,
                wallet.getStatus() == WalletStatus.ACTIVE ? "Wallet unfrozen successfully" : "Manual freeze removed; wallet remains frozen because it is blacklisted");
    }

    public Wallet findById(Long id) {
        return walletRepository.findById(id).orElseThrow(() -> new WalletNotFoundException(id));
    }

    public Wallet findByIdForUpdate(Long id) {
        return walletRepository.findByIdForUpdate(id).orElseThrow(() -> new WalletNotFoundException(id));
    }

    /** Locks two wallets in ascending ID order to avoid A->B/B->A deadlocks. */
    public Map<Long, Wallet> lockWalletsInOrder(Long firstWalletId, Long secondWalletId) {
        if (firstWalletId == null || secondWalletId == null) throw new InvalidTransactionException("Wallet ids are required");
        List<Long> ids = firstWalletId.equals(secondWalletId)
                ? List.of(firstWalletId)
                : List.of(Math.min(firstWalletId, secondWalletId), Math.max(firstWalletId, secondWalletId));
        List<Wallet> wallets = walletRepository.findAllByIdInOrderForUpdate(ids);
        if (wallets.size() != ids.size()) throw new WalletNotFoundException(wallets.size() == 0 ? ids.get(0) : ids.get(ids.size()-1));
        Map<Long, Wallet> result = new HashMap<>();
        wallets.forEach(w -> result.put(w.getId(), w));
        return result;
    }

    public Wallet credit(Long walletId, BigDecimal amount) {
        return creditLocked(findByIdForUpdate(walletId), amount);
    }

    public Wallet debit(Long walletId, BigDecimal amount) {
        return debitLocked(findByIdForUpdate(walletId), amount);
    }

    public Wallet creditLocked(Wallet wallet, BigDecimal amount) {
        ensureMovable(wallet);
        requirePositive(amount, "Credit");
        wallet.setBalance(wallet.getBalance().add(amount));
        return walletRepository.save(wallet);
    }

    public Wallet debitLocked(Wallet wallet, BigDecimal amount) {
        ensureMovable(wallet);
        requirePositive(amount, "Debit");
        if (amount.compareTo(wallet.getBalance()) > 0) {
            throw new InsufficentAmountException("Insufficient funds. Wallet balance is " + wallet.getBalance());
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        return walletRepository.save(wallet);
    }

    public void save(Wallet wallet) {
        walletRepository.save(wallet);
    }

    private void ensureMovable(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) throw new WalletFrozenException("Wallet is not active");
    }

    private void requirePositive(BigDecimal amount, String operation) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AmountNegativeException(operation + " amount must be greater than zero");
        }
    }
}
