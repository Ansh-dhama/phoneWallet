package com.example.phoneWallet.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String topic, String payload) {
        kafkaTemplate.send(topic, payload);
    }
}