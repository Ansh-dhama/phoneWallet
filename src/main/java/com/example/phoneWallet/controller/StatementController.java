package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.StatementResponse;
import com.example.phoneWallet.services.StatementService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statements")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    // GET /api/statements/wallet/{walletId}
    @GetMapping("/wallet/{walletId}")
    public ResponseEntity<List<StatementResponse>> getStatementsByWalletId(
            @PathVariable Long walletId
    ) {
        List<StatementResponse> response =
                statementService.getStatementsByWalletId(walletId);

        return ResponseEntity.ok(response);
    }

    // GET /api/statements/wallet/{walletId}/range?startDate=2026-06-01T00:00:00&endDate=2026-06-30T23:59:59
    @GetMapping("/wallet/{walletId}/range")
    public ResponseEntity<List<StatementResponse>> getStatementByDateRange(
            @PathVariable Long walletId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        List<StatementResponse> response =
                statementService.getStatementByDateRange(walletId, startDate, endDate);

        return ResponseEntity.ok(response);
    }
}