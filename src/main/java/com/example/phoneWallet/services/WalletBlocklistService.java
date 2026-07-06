package com.example.phoneWallet.services;

import com.example.phoneWallet.Exceptions.TransactionBlock;
import com.example.phoneWallet.Exceptions.WalletFrozenException;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.entity.WalletBlocklist;
import com.example.phoneWallet.Repository.WalletBlocklistRepository;
import com.example.phoneWallet.enums.WalletStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class WalletBlocklistService {

    private final WalletBlocklistRepository walletBlocklistRepository;
    private final WalletService walletService;

    public WalletBlocklistService(WalletBlocklistRepository walletBlocklistRepository,
                                  WalletService walletService) {
        this.walletBlocklistRepository = walletBlocklistRepository;
        this.walletService = walletService;
    }

    @Transactional
    public WalletBlocklist blocklistWallet(Long walletId, String reason, String blacklistedBy) {
Optional<WalletBlocklist> walletBlocklistOptional = walletBlocklistRepository.findById(walletId);
   if(walletBlocklistOptional.isPresent()) {
       throw new WalletFrozenException("wallet already frozen / block");
   }
        Wallet wallet = walletService.findById(walletId);
        wallet.setStatus(WalletStatus.FROZEN);
        walletService.save(wallet);
        WalletBlocklist existing = walletBlocklistRepository.findByWalletId(walletId)
                .orElse(null);

        if (existing != null) {
            existing.setActive(true);
            existing.setReason(reason);
            existing.setBlacklistedBy(blacklistedBy);
            return walletBlocklistRepository.save(existing);
        }

        WalletBlocklist blacklist = new WalletBlocklist();
        blacklist.setWalletId(wallet.getId());
        blacklist.setReason(reason);
        blacklist.setBlacklistedBy(blacklistedBy);
        blacklist.setActive(true);

        return walletBlocklistRepository.save(blacklist);
    }

    @Transactional
    public void unblocklistWallet(Long walletId) {

        WalletBlocklist blacklist = walletBlocklistRepository.findByWalletIdAndActiveTrue(walletId)
                .orElseThrow(() -> new TransactionBlock("Wallet is not blacklisted"));
        Wallet wallet = walletService.findById(walletId);
        wallet.setStatus(WalletStatus.ACTIVE);
        blacklist.setActive(false);

        walletBlocklistRepository.save(blacklist);
    }

    public boolean isBlacklisted(Long walletId) {
        return walletBlocklistRepository.existsByWalletIdAndActiveTrue(walletId);
    }
}