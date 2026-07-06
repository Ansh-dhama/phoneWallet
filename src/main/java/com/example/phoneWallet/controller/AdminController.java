package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.AdminDashboardSummaryResponse;
import com.example.phoneWallet.entity.*;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import com.example.phoneWallet.services.AdminDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminDashboardService adminDashboardService;

    public AdminController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/dashboard/summary")
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return adminDashboardService.getDashboardSummary();
    }

    @GetMapping("/wallets")
    public List<Wallet> getAllWallets() {
        return adminDashboardService.getAllWallets();
    }

    @GetMapping("/wallets/frozen")
    public List<Wallet> getFrozenWallets() {
        return adminDashboardService.getFrozenWallets();
    }

    @GetMapping("/wallets/blacklisted")
    public List<WalletBlocklist> getBlacklistedWallets() {
        return adminDashboardService.getBlacklistedWallets();
    }

    @GetMapping("/transactions")
    public List<Transaction> getAllTransactions() {
        return adminDashboardService.getAllTransactions();
    }

    @GetMapping("/transactions/failed")
    public List<Transaction> getFailedTransactions() {
        return adminDashboardService.getFailedTransactions();
    }

    @GetMapping("/transactions/status/{status}")
    public List<Transaction> getTransactionsByStatus(
            @PathVariable TransactionStatus status
    ) {
        return adminDashboardService.getTransactionsByStatus(status);
    }

    @GetMapping("/transactions/type/{type}")
    public List<Transaction> getTransactionsByType(
            @PathVariable TransactionType type
    ) {
        return adminDashboardService.getTransactionsByType(type);
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> getAllAuditLogs() {
        return adminDashboardService.getAllAuditLogs();
    }

    @GetMapping("/notifications")
    public List<Notification> getAllNotifications() {
        return adminDashboardService.getAllNotifications();
    }
}