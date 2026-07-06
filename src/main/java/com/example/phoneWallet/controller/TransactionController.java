package com.example.phoneWallet.controller;

import com.example.phoneWallet.Mapper.TransactionMapper;
import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.services.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // POST /api/transactions/transfer
    // USER
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        Transaction tx = transactionService.processTransaction(request);
        return ResponseEntity.ok(TransactionMapper.toResponse(tx));
    }

    // POST /api/transactions/pay
    // USER
    @PostMapping("/pay")
    public ResponseEntity<TransactionResponse> pay(
            @Valid @RequestBody PayRequest request
    ) {
        Transaction tx = transactionService.payTransaction(request);
        return ResponseEntity.ok(TransactionMapper.toResponse(tx));
    }

    // POST /api/transactions/refund
    // ADMIN / MERCHANT
    @PostMapping("/refund")
    public ResponseEntity<TransactionResponse> refund(
            @Valid @RequestBody RefundRequest request
    ) {
        Transaction tx = transactionService.refund(request);
        return ResponseEntity.ok(TransactionMapper.toResponse(tx));
    }

    // GET /api/transactions/{transactionId}
    // USER / MERCHANT / ADMIN
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable Long transactionId
    ) {
        Transaction tx = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(TransactionMapper.toResponse(tx));
    }

    // GET /api/transactions/wallet/{walletId}
    // USER / MERCHANT / ADMIN
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<TransactionResponse>> getWalletTransactions(
            @PathVariable Long walletId
    ) {
        List<Transaction> transactions = transactionService.getWalletTransactions(walletId);

        List<TransactionResponse> response = transactions.stream()
                .map(TransactionMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    // POST /api/transactions/reversal
// ADMIN
    @PostMapping("/reversal")
    public ResponseEntity<TransactionResponse> reverseTransaction(
            @Valid @RequestBody ReversalRequest request
    ) {
        Transaction tx = transactionService.reverseTransaction(request);
        return ResponseEntity.ok(TransactionMapper.toResponse(tx));
    }
}