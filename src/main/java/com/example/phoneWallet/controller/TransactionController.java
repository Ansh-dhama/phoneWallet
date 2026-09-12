package com.example.phoneWallet.controller;

import com.example.phoneWallet.Mapper.TransactionMapper;
import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) { this.transactionService = transactionService; }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(TransactionMapper.toResponse(transactionService.processTransaction(request)));
    }

    @PostMapping("/pay")
    public ResponseEntity<TransactionResponse> pay(@Valid @RequestBody PayRequest request) {
        return ResponseEntity.ok(TransactionMapper.toResponse(transactionService.payTransaction(request)));
    }

    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refund(@Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(TransactionMapper.toResponse(transactionService.refund(request)));
    }

    @PostMapping("/reversal")
    public ResponseEntity<TransactionResponse> reverse(@Valid @RequestBody ReversalRequest request) {
        return ResponseEntity.ok(TransactionMapper.toResponse(transactionService.reverseTransaction(request)));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> get(@PathVariable Long transactionId) {
        return ResponseEntity.ok(TransactionMapper.toResponse(transactionService.getTransaction(transactionId)));
    }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<PageResponse<TransactionResponse>> walletTransactions(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionResponse> mapped = transactionService.getWalletTransactions(
                walletId,
                PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"))
        ).map(TransactionMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(mapped));
    }
}
