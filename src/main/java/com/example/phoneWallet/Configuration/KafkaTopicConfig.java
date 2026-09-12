package com.example.phoneWallet.Configuration;

import com.example.phoneWallet.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "wallet.kafka.topics.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaTopicConfig {
    private final int replicationFactor;

    public KafkaTopicConfig(@Value("${wallet.kafka.replication-factor:1}") int replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name).partitions(3).replicas(replicationFactor).build();
    }

    @Bean public NewTopic transactionSuccessTopic() { return topic(KafkaTopics.TRANSACTION_SUCCESS); }
    @Bean public NewTopic transactionFailedTopic() { return topic(KafkaTopics.TRANSACTION_FAILED); }
    @Bean public NewTopic refundCompletedTopic() { return topic(KafkaTopics.REFUND_COMPLETED); }
    @Bean public NewTopic notificationRequestedTopic() { return topic(KafkaTopics.NOTIFICATION_REQUESTED); }
}
