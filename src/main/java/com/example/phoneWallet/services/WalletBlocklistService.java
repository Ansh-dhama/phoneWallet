package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.TransactionBlock;
import com.example.phoneWallet.Exceptions.WalletFrozenException;
import com.example.phoneWallet.Repository.WalletBlocklistRepository;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.entity.WalletBlocklist;
import com.example.phoneWallet.enums.WalletStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WalletBlocklistService {
    private final WalletBlocklistRepository repository;
    private final WalletService walletService;

    public WalletBlocklistService(WalletBlocklistRepository repository, WalletService walletService) {
        this.repository = repository;
        this.walletService = walletService;
    }

    @Transactional
    public WalletBlocklist blocklistWallet(Long walletId, String reason, String blacklistedBy) {
        if (repository.findByWalletIdAndActiveTrue(walletId).isPresent()) {
            throw new WalletFrozenException("Wallet is already blacklisted");
        }
        Wallet wallet = walletService.findByIdForUpdate(walletId);
        wallet.setStatus(WalletStatus.FROZEN);
        walletService.save(wallet);

        WalletBlocklist record = repository.findByWalletId(walletId).orElseGet(WalletBlocklist::new);
        record.setWalletId(walletId);
        record.setReason(reason);
        record.setBlacklistedBy(blacklistedBy);
        record.setActive(true);
        return repository.save(record);
    }

    @Transactional
    public void unblocklistWallet(Long walletId) {
        WalletBlocklist record = repository.findByWalletIdAndActiveTrue(walletId)
                .orElseThrow(() -> new TransactionBlock("Wallet is not blacklisted"));
        Wallet wallet = walletService.findByIdForUpdate(walletId);
        record.setActive(false);
        repository.save(record);

        // Do not undo an independent manual freeze.
        wallet.setStatus(wallet.isAdministrativelyFrozen() ? WalletStatus.FROZEN : WalletStatus.ACTIVE);
        walletService.save(wallet);
    }

    public boolean isBlacklisted(Long walletId) {
        return repository.existsByWalletIdAndActiveTrue(walletId);
    }
}
