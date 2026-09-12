package com.example.phoneWallet.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDashboardSummaryResponse {
    private long totalWallets;
    private long activeWallets;
    private long frozenWallets;
    private long blacklistedWallets;
    private long totalTransactions;
    private long successfulTransactions;
    private long partiallyRefundedTransactions;
    private long failedTransactions;
    private long refundedTransactions;
    private long reversedTransactions;
    private long totalNotifications;
    private long totalAuditLogs;
}
