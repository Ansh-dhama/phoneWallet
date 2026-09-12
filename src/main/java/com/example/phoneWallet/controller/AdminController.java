package com.example.phoneWallet.controller;

import com.example.phoneWallet.dto.*;
import com.example.phoneWallet.entity.*;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import com.example.phoneWallet.services.AdminDashboardService;
import com.example.phoneWallet.services.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminDashboardService dashboardService;
    private final AdminUserService adminUserService;

    public AdminController(AdminDashboardService dashboardService, AdminUserService adminUserService) {
        this.dashboardService = dashboardService;
        this.adminUserService = adminUserService;
    }

    @GetMapping("/dashboard/summary")
    public AdminDashboardSummaryResponse summary() { return dashboardService.getDashboardSummary(); }

    @GetMapping("/wallets")
    public PageResponse<Wallet> wallets(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getAllWallets(pageable(page, size)));
    }

    @GetMapping("/wallets/frozen")
    public PageResponse<Wallet> frozen(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getFrozenWallets(pageable(page, size)));
    }

    @GetMapping("/wallets/blacklisted")
    public PageResponse<WalletBlocklist> blacklisted(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getBlacklistedWallets(pageable(page, size)));
    }

    @GetMapping("/transactions")
    public PageResponse<Transaction> transactions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getAllTransactions(pageable(page, size)));
    }

    @GetMapping("/transactions/failed")
    public PageResponse<Transaction> failed(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getFailedTransactions(pageable(page, size)));
    }

    @GetMapping("/transactions/status/{status}")
    public PageResponse<Transaction> byStatus(@PathVariable TransactionStatus status,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getTransactionsByStatus(status, pageable(page, size)));
    }

    @GetMapping("/transactions/type/{type}")
    public PageResponse<Transaction> byType(@PathVariable TransactionType type,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getTransactionsByType(type, pageable(page, size)));
    }

    @GetMapping("/audit-logs")
    public PageResponse<AuditLog> audit(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getAllAuditLogs(pageable(page, size)));
    }

    @GetMapping("/notifications")
    public PageResponse<Notification> notifications(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(dashboardService.getAllNotifications(pageable(page, size)));
    }

    @GetMapping("/users")
    public PageResponse<UserSummaryResponse> users(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return adminUserService.listUsers(page, size);
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<UserSummaryResponse> updateRole(@PathVariable Long userId, @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(adminUserService.updateRole(userId, request.role()));
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(Sort.Direction.DESC, "id"));
    }
}
