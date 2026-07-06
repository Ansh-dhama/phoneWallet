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

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification sendTransactionSuccessNotification(TransactionEvent event) {

        Notification notification = new Notification();
        notification.setTransactionReference(event.getTransactionReference());
        notification.setWalletId(event.getFromWalletId());
        notification.setNotificationType(NotificationType.TRANSACTION_SUCCESS);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setMessage(
                "Transaction successful. Reference: " + event.getTransactionReference()
                        + ", Amount: " + event.getAmount()
                        + " " + event.getCurrency()
        );

        notification = notificationRepository.save(notification);

        try {
            System.out.println("========== NOTIFICATION SENT ==========");
            System.out.println(notification.getMessage());
            System.out.println("=======================================");

            notification.setStatus(NotificationStatus.SENT);
            notification.setFailureReason(null);

        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(ex.getMessage());
        }

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification sendRefundCompletedNotification(TransactionEvent event) {

        Notification notification = new Notification();
        notification.setTransactionReference(event.getTransactionReference());
        notification.setWalletId(event.getToWalletId());
        notification.setNotificationType(NotificationType.REFUND_COMPLETED);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setMessage(
                "Refund completed. Reference: " + event.getTransactionReference()
                        + ", Amount: " + event.getAmount()
                        + " " + event.getCurrency()
        );

        notification = notificationRepository.save(notification);

        try {
            System.out.println("========== REFUND NOTIFICATION SENT ==========");
            System.out.println(notification.getMessage());
            System.out.println("=============================================");

            notification.setStatus(NotificationStatus.SENT);
            notification.setFailureReason(null);

        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(ex.getMessage());
        }

        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification sendTransactionFailedNotification(TransactionEvent event) {

        Notification notification = new Notification();
        notification.setTransactionReference(event.getTransactionReference());
        notification.setWalletId(event.getFromWalletId());
        notification.setNotificationType(NotificationType.TRANSACTION_FAILED);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setMessage(
                "Transaction failed. Reference: " + event.getTransactionReference()
                        + ", Amount: " + event.getAmount()
                        + " " + event.getCurrency()
        );

        notification = notificationRepository.save(notification);

        try {
            System.out.println("========== FAILED TRANSACTION NOTIFICATION SENT ==========");
            System.out.println(notification.getMessage());
            System.out.println("=========================================================");

            notification.setStatus(NotificationStatus.SENT);
            notification.setFailureReason(null);

        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailureReason(ex.getMessage());
        }

        return notificationRepository.save(notification);
    }

}