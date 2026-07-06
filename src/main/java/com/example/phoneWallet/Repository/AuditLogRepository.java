package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.AuditLog;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.AuditStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByWalletId(Long walletId);

    List<AuditLog> findByTransactionReference(String transactionReference);

    List<AuditLog> findByAction(AuditAction action);

    List<AuditLog> findByStatus(AuditStatus status);
}