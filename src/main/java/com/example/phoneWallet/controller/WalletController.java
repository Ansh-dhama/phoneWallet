package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.BlacklistWalletRequest;
import com.example.phoneWallet.dto.LoadMoneyRequest;
import com.example.phoneWallet.dto.TransactionResponse;
import com.example.phoneWallet.dto.WalletRequestDTO;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.entity.Wallet;
import com.example.phoneWallet.Mapper.TransactionMapper;
import com.example.phoneWallet.services.TransactionService;
import com.example.phoneWallet.services.WalletBlocklistService;
import com.example.phoneWallet.services.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;
    private final WalletBlocklistService walletBlocklistService;

    public WalletController(WalletService walletService,
                            TransactionService transactionService,
                            WalletBlocklistService walletBlocklistService) {
        this.walletService = walletService;
        this.transactionService = transactionService;
        this.walletBlocklistService = walletBlocklistService;
    }

    // POST /api/wallets
    // USER / MERCHANT
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody WalletRequestDTO request) {
        Wallet wallet = walletService.createWallet(request);
        return ResponseEntity.ok(wallet);
    }

    // GET /api/wallets/{walletId}
    // USER / MERCHANT / ADMIN
    @GetMapping("/{walletId}")
    public ResponseEntity<Wallet> findById(@PathVariable Long walletId) {
        Wallet wallet = walletService.findById(walletId);
        return ResponseEntity.ok(wallet);
    }

    // GET /api/wallets/{walletId}/balance
    // USER / MERCHANT / ADMIN
    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long walletId) {
        BigDecimal balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(balance);
    }

    // POST /api/wallets/{walletId}/load-money
    // USER
    @PostMapping("/{walletId}/load-money")
    public ResponseEntity<TransactionResponse> loadMoney(
            @PathVariable Long walletId,
            @Valid @RequestBody LoadMoneyRequest request
    ) {
        Transaction transaction = transactionService.loadMoney(request, walletId);
        return ResponseEntity.ok(TransactionMapper.toResponse(transaction));
    }

    // POST /api/wallets/{walletId}/freeze
    // ADMIN
    @PostMapping("/{walletId}/freeze")
    public ResponseEntity<String> freezeWallet(@PathVariable Long walletId) {
        walletService.freezeWallet(walletId);
        return ResponseEntity.ok("Wallet frozen successfully");
    }

    // POST /api/wallets/{walletId}/unfreeze
    // ADMIN
    @PostMapping("/{walletId}/unfreeze")
    public ResponseEntity<String> unfreezeWallet(@PathVariable Long walletId) {
        walletService.unfreezeWallet(walletId);
        return ResponseEntity.ok("Wallet unfrozen successfully");
    }

    // POST /api/wallets/{walletId}/blacklist
    // ADMIN
    @PostMapping("/{walletId}/blacklist")
    public ResponseEntity<String> blacklistWallet(
            @PathVariable Long walletId,
            @Valid @RequestBody BlacklistWalletRequest request
    ) {
        walletBlocklistService.blocklistWallet(
                walletId,
                request.getReason(),
                request.getBlacklistedBy()
        );

        return ResponseEntity.ok("Wallet blacklisted successfully");
    }

    // POST /api/wallets/{walletId}/unblacklist
    // ADMIN
    @PostMapping("/{walletId}/unblacklist")
    public ResponseEntity<String> unblacklistWallet(@PathVariable Long walletId) {
        walletBlocklistService.unblocklistWallet(walletId);
        return ResponseEntity.ok("Wallet removed from blacklist successfully");
    }
}