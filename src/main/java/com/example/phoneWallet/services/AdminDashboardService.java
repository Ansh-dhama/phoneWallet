package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.*;
import com.example.phoneWallet.dto.AdminDashboardSummaryResponse;
import com.example.phoneWallet.entity.*;
import com.example.phoneWallet.enums.TransactionStatus;
import com.example.phoneWallet.enums.TransactionType;
import com.example.phoneWallet.enums.WalletStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final WalletBlocklistRepository walletBlocklistRepository;

    public AdminDashboardService(WalletRepository walletRepository, TransactionRepository transactionRepository,
                                 AuditLogRepository auditLogRepository, NotificationRepository notificationRepository,
                                 WalletBlocklistRepository walletBlocklistRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.walletBlocklistRepository = walletBlocklistRepository;
    }

    public AdminDashboardSummaryResponse getDashboardSummary() {
        AdminDashboardSummaryResponse r = new AdminDashboardSummaryResponse();
        r.setTotalWallets(walletRepository.count());
        r.setActiveWallets(walletRepository.countByStatus(WalletStatus.ACTIVE));
        r.setFrozenWallets(walletRepository.countByStatus(WalletStatus.FROZEN));
        r.setBlacklistedWallets(walletBlocklistRepository.countByActiveTrue());
        r.setTotalTransactions(transactionRepository.count());
        r.setSuccessfulTransactions(transactionRepository.countByStatus(TransactionStatus.SUCCESS));
        r.setPartiallyRefundedTransactions(transactionRepository.countByStatus(TransactionStatus.PARTIALLY_REFUNDED));
        r.setFailedTransactions(transactionRepository.countByStatus(TransactionStatus.FAILED));
        r.setRefundedTransactions(transactionRepository.countByStatus(TransactionStatus.REFUNDED));
        r.setReversedTransactions(transactionRepository.countByStatus(TransactionStatus.REVERSED));
        r.setTotalNotifications(notificationRepository.count());
        r.setTotalAuditLogs(auditLogRepository.count());
        return r;
    }

    public Page<Wallet> getAllWallets(Pageable pageable) { return walletRepository.findAll(pageable); }
    public Page<Wallet> getFrozenWallets(Pageable pageable) { return walletRepository.findByStatus(WalletStatus.FROZEN, pageable); }
    public Page<WalletBlocklist> getBlacklistedWallets(Pageable pageable) { return walletBlocklistRepository.findByActiveTrue(pageable); }
    public Page<Transaction> getAllTransactions(Pageable pageable) { return transactionRepository.findAll(pageable); }
    public Page<Transaction> getFailedTransactions(Pageable pageable) { return transactionRepository.findByStatus(TransactionStatus.FAILED, pageable); }
    public Page<Transaction> getTransactionsByStatus(TransactionStatus status, Pageable pageable) { return transactionRepository.findByStatus(status, pageable); }
    public Page<Transaction> getTransactionsByType(TransactionType type, Pageable pageable) { return transactionRepository.findByType(type, pageable); }
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) { return auditLogRepository.findAll(pageable); }
    public Page<Notification> getAllNotifications(Pageable pageable) { return notificationRepository.findAll(pageable); }
}
