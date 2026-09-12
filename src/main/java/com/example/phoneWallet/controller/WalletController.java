package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.BlacklistWalletRequest;
import com.example.phoneWallet.dto.WalletRequestDTO;
import com.example.phoneWallet.entity.User;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.services.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletService walletService;
    private final WalletBlocklistService blocklistService;
    private final AccessControlService accessControlService;

    public WalletController(WalletService walletService, WalletBlocklistService blocklistService,
                            AccessControlService accessControlService) {
        this.walletService = walletService;
        this.blocklistService = blocklistService;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody WalletRequestDTO request) {
        return ResponseEntity.ok(walletService.createWallet(request));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<Wallet>> mine() {
        return ResponseEntity.ok(walletService.getMyWallets());
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> findById(@PathVariable Long walletId) {
        accessControlService.requireWalletAccess(walletId);
        return ResponseEntity.ok(walletService.findById(walletId));
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long walletId) {
        accessControlService.requireWalletAccess(walletId);
        return ResponseEntity.ok(walletService.getBalance(walletId));
    }

    /** Direct wallet minting is intentionally disabled. Use /api/topups. */
    @PostMapping("/{walletId}/load-money")
    public ResponseEntity<String> deprecatedLoadMoney(@PathVariable Long walletId) {
        accessControlService.requireOwnedWallet(walletId);
        return ResponseEntity.status(HttpStatus.GONE)
                .body("Direct load-money is disabled. Create a verified top-up intent via /api/topups/wallet/{walletId}.");
    }

    @PostMapping("/{walletId}/freeze")
    public ResponseEntity<String> freezeWallet(@PathVariable Long walletId) {
        walletService.freezeWallet(walletId);
        return ResponseEntity.ok("Wallet frozen successfully");
    }

    @PostMapping("/{walletId}/unfreeze")
    public ResponseEntity<String> unfreezeWallet(@PathVariable Long walletId) {
        walletService.unfreezeWallet(walletId);
        return ResponseEntity.ok("Manual wallet freeze removed");
    }

    @PostMapping("/{walletId}/blacklist")
    public ResponseEntity<String> blacklistWallet(@PathVariable Long walletId,
                                                  @Valid @RequestBody BlacklistWalletRequest request) {
        User admin = accessControlService.currentUser();
        blocklistService.blocklistWallet(walletId, request.getReason(), admin.getUsername());
        return ResponseEntity.ok("Wallet blacklisted successfully");
    }

    @PostMapping("/{walletId}/unblacklist")
    public ResponseEntity<String> unblacklistWallet(@PathVariable Long walletId) {
        blocklistService.unblocklistWallet(walletId);
        return ResponseEntity.ok("Wallet removed from blacklist successfully");
    }
}
