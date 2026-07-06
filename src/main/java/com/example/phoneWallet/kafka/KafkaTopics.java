package com.example.phoneWallet.kafka;

import com.example.phoneWallet.dto.TransactionEvent;
import com.example.phoneWallet.entity.Transaction;
import com.example.phoneWallet.kafka.KafkaTopics;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


public class KafkaTopics {

    public static final String TRANSACTION_SUCCESS = "transaction.success";
    public static final String TRANSACTION_FAILED = "transaction.failed";
    public static final String REFUND_COMPLETED = "refund.completed";
    public static final String NOTIFICATION_REQUESTED = "notification.requested";

    private KafkaTopics() {
    }
}