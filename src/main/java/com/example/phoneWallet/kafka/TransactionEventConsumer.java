package com.example.phoneWallet.kafka;

import com.example.phoneWallet.dto.TransactionEvent;
import com.example.phoneWallet.services.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TransactionEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public TransactionEventConsumer(NotificationService notificationService,
                                    ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "transaction.success",
            groupId = "wallet-notification-group"
    )
    public void consumeTransactionSuccess(String payload) {

        try {
            TransactionEvent event =
                    objectMapper.readValue(payload, TransactionEvent.class);

            notificationService.sendTransactionSuccessNotification(event);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to consume transaction success event", ex);
        }
    }

    @KafkaListener(
            topics = "refund.completed",
            groupId = "wallet-notification-group"
    )
    public void consumeRefundCompleted(String payload) {

        try {
            TransactionEvent event =
                    objectMapper.readValue(payload, TransactionEvent.class);

            notificationService.sendRefundCompletedNotification(event);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to consume refund completed event", ex);
        }
    }
}