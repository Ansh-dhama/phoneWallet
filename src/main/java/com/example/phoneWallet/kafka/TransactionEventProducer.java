package com.example.phoneWallet.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TransactionEventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public TransactionEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /** Returns only after the broker acknowledges the event. */
    public void publishAndWait(String topic, String payload) throws Exception {
        kafkaTemplate.send(topic, payload).get(10, TimeUnit.SECONDS);
    }
}
