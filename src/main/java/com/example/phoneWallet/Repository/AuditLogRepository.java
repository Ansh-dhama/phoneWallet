package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.AuditLog;
import com.example.phoneWallet.enums.AuditAction;
import com.example.phoneWallet.enums.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);
    Page<AuditLog> findByWalletId(Long walletId, Pageable pageable);
    Page<AuditLog> findByTransactionReference(String transactionReference, Pageable pageable);
    Page<AuditLog> findByAction(AuditAction action, Pageable pageable);
    Page<AuditLog> findByStatus(AuditStatus status, Pageable pageable);
}
