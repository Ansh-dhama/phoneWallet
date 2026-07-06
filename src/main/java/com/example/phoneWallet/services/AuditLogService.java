package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.AuditLogRepository;
import com.example.phoneWallet.entity.AuditLog;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.AuditStatus;
import com.example.phoneWallet.enums.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(AuditAction action,
                           Long userId,
                           String username,
                           Role role,
                           Long walletId,
                           String transactionReference,
                           String description) {

        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setRole(role);
        auditLog.setAction(action);
        auditLog.setStatus(AuditStatus.SUCCESS);
        auditLog.setWalletId(walletId);
        auditLog.setTransactionReference(transactionReference);
        auditLog.setDescription(description);
        auditLog.setFailureReason(null);

        auditLogRepository.save(auditLog);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(AuditAction action,
                           Long userId,
                           String username,
                           Role role,
                           Long walletId,
                           String transactionReference,
                           String description,
                           String failureReason) {

        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setUsername(username);
        auditLog.setRole(role);
        auditLog.setAction(action);
        auditLog.setStatus(AuditStatus.FAILED);
        auditLog.setWalletId(walletId);
        auditLog.setTransactionReference(transactionReference);
        auditLog.setDescription(description);
        auditLog.setFailureReason(failureReason);

        auditLogRepository.save(auditLog);
    }
}