package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.NotificationRepository;
import com.example.phoneWallet.dto.TransactionEvent;
import com.example.phoneWallet.entity.Notification;
import com.example.phoneWallet.enums.NotificationStatus;
import com.example.phoneWallet.enums.NotificationType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) { this.repository = repository; }

    @Transactional
    public Notification sendTransactionSuccessNotification(TransactionEvent event) {
        Long walletId = event.getToWalletId() != null ? event.getToWalletId() : event.getFromWalletId();
        return createOnce(event, walletId, NotificationType.TRANSACTION_SUCCESS,
                "Transaction successful. Reference: " + event.getTransactionReference() + ", Amount: " + event.getAmount() + " " + event.getCurrency());
    }

    @Transactional
    public Notification sendRefundCompletedNotification(TransactionEvent event) {
        return createOnce(event, event.getToWalletId(), NotificationType.REFUND_COMPLETED,
                "Refund completed. Reference: " + event.getTransactionReference() + ", Amount: " + event.getAmount() + " " + event.getCurrency());
    }

    @Transactional
    public Notification sendTransactionFailedNotification(TransactionEvent event) {
        return createOnce(event, event.getFromWalletId(), NotificationType.TRANSACTION_FAILED,
                "Transaction failed. Reference: " + event.getTransactionReference() + ", Amount: " + event.getAmount() + " " + event.getCurrency());
    }

    private Notification createOnce(TransactionEvent event, Long walletId, NotificationType type, String message) {
        repository.insertIfAbsent(
                event.getTransactionReference(),
                walletId,
                type.name(),
                message,
                NotificationStatus.SENT.name()
        );
        return repository.findByTransactionReferenceAndNotificationType(event.getTransactionReference(), type)
                .orElseThrow(() -> new IllegalStateException("Notification insert completed but row could not be read"));
    }
}
