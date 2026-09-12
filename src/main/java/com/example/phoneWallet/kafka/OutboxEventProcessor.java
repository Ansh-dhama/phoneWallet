package com.example.phoneWallet.kafka;

import com.example.phoneWallet.entity.OutboxEvent;
import com.example.phoneWallet.services.OutboxEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(name = "wallet.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxEventProcessor {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventProcessor.class);
    private final OutboxEventService outboxEventService;
    private final TransactionEventProducer producer;

    public OutboxEventProcessor(OutboxEventService outboxEventService, TransactionEventProducer producer) {
        this.outboxEventService = outboxEventService;
        this.producer = producer;
    }

    @Scheduled(fixedDelayString = "${wallet.outbox.poll-ms:1000}")
    public void processPendingEvents() {
        List<OutboxEvent> events = outboxEventService.claimPendingBatch();
        for (OutboxEvent event : events) {
            try {
                producer.publishAndWait(event.getTopic(), event.getPayload());
                outboxEventService.markSent(event.getId());
            } catch (Exception ex) {
                log.warn("Kafka publish failed for outbox event {}", event.getEventId(), ex);
                outboxEventService.markPublishFailure(event.getId(), ex.getMessage());
            }
        }
    }

    @Scheduled(fixedDelayString = "${wallet.outbox.recovery-ms:30000}")
    public void recoverStaleClaims() {
        outboxEventService.recoverStaleProcessing(LocalDateTime.now().minusMinutes(2));
    }
}
