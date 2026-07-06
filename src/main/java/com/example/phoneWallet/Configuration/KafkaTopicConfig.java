package com.example.phoneWallet.Configuration;

import com.example.phoneWallet.kafka.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic transactionSuccessTopic() {
        return TopicBuilder.name(KafkaTopics.TRANSACTION_SUCCESS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionFailedTopic() {
        return TopicBuilder.name(KafkaTopics.TRANSACTION_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundCompletedTopic() {
        return TopicBuilder.name(KafkaTopics.REFUND_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationRequestedTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_REQUESTED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}