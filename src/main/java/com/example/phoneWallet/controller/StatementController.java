package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.PageResponse;
import com.example.phoneWallet.dto.StatementResponse;
import com.example.phoneWallet.services.StatementService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/statements")
public class StatementController {
    private final StatementService statementService;

    public StatementController(StatementService statementService) { this.statementService = statementService; }

    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<PageResponse<StatementResponse>> byWallet(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(statementService.getStatementsByWalletId(walletId,
                PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/wallet/{walletId}/range")
    public ResponseEntity<PageResponse<StatementResponse>> byRange(
            @PathVariable Long walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(statementService.getStatementByDateRange(walletId, startDate, endDate,
                PageRequest.of(page, Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "createdAt"))));
    }
}
