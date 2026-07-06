package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.*;
import com.example.phoneWallet.dto.AdminDashboardSummaryResponse;
import com.example.phoneWallet.entity.*;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import com.example.phoneWallet.enums.WalletStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDashboardService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final WalletBlocklistRepository walletBlocklistRepository;

    public AdminDashboardService(WalletRepository walletRepository,
                                 TransactionRepository transactionRepository,
                                 AuditLogRepository auditLogRepository,
                                 NotificationRepository notificationRepository,
                                 WalletBlocklistRepository walletBlocklistRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.walletBlocklistRepository = walletBlocklistRepository;
    }

    public AdminDashboardSummaryResponse getDashboardSummary() {

        AdminDashboardSummaryResponse response = new AdminDashboardSummaryResponse();

        response.setTotalWallets(walletRepository.count());
        response.setActiveWallets(walletRepository.countByStatus(WalletStatus.ACTIVE));
        response.setFrozenWallets(walletRepository.countByStatus(WalletStatus.FROZEN));

        response.setBlacklistedWallets(walletBlocklistRepository.count());

        response.setTotalTransactions(transactionRepository.count());
        response.setSuccessfulTransactions(transactionRepository.countByStatus(TransactionStatus.SUCCESS));
        response.setFailedTransactions(transactionRepository.countByStatus(TransactionStatus.FAILED));
        response.setRefundedTransactions(transactionRepository.countByStatus(TransactionStatus.REFUNDED));
        response.setReversedTransactions(transactionRepository.countByStatus(TransactionStatus.REVERSED));

        response.setTotalNotifications(notificationRepository.count());
        response.setTotalAuditLogs(auditLogRepository.count());

        return response;
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public List<Wallet> getFrozenWallets() {
        return walletRepository.findByStatus(WalletStatus.FROZEN);
    }

    public List<WalletBlocklist> getBlacklistedWallets() {
        return walletBlocklistRepository.findAll();
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getFailedTransactions() {
        return transactionRepository.findByStatus(TransactionStatus.FAILED);
    }

    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }

    public List<Transaction> getTransactionsByType(TransactionType type) {
        return transactionRepository.findByType(type);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}