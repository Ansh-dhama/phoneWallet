package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.Notification;
import com.example.phoneWallet.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByWalletId(Long walletId, Pageable pageable);
    Optional<Notification> findByTransactionReferenceAndNotificationType(
            String transactionReference, NotificationType notificationType
    );

    @Modifying
    @Query(value = """
            INSERT IGNORE INTO notifications
              (transaction_reference, wallet_id, notification_type, message, status, created_at, updated_at)
            VALUES
              (:transactionReference, :walletId, :notificationType, :message, :status, NOW(), NOW())
            """, nativeQuery = true)
    int insertIfAbsent(@Param("transactionReference") String transactionReference,
                       @Param("walletId") Long walletId,
                       @Param("notificationType") String notificationType,
                       @Param("message") String message,
                       @Param("status") String status);
}
