package com.example.phoneWallet.kafka;

import com.example.phoneWallet.Repository.OutboxEventRepository;
import com.example.phoneWallet.entity.OutboxEvent;
import com.example.phoneWallet.enums.OutboxStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxEventProcessor {

    private static final int MAX_RETRY_COUNT = 5;

    private final OutboxEventRepository outboxEventRepository;
    private final TransactionEventProducer transactionEventProducer;

    public OutboxEventProcessor(OutboxEventRepository outboxEventRepository,
                                TransactionEventProducer transactionEventProducer) {
        this.outboxEventRepository = outboxEventRepository;
        this.transactionEventProducer = transactionEventProducer;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processPendingEvents() {

        List<OutboxEvent> pendingEvents =
                outboxEventRepository.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxEvent event : pendingEvents) {
            processSingleEvent(event);
        }
    }

    @Scheduled(fixedDelay = 15000)
    @Transactional
    public void retryFailedEvents() {

        List<OutboxEvent> failedEvents =
                outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.FAILED);

        for (OutboxEvent event : failedEvents) {

            if (event.getRetryCount() < MAX_RETRY_COUNT) {
                processSingleEvent(event);
            }
        }
    }

    private void processSingleEvent(OutboxEvent event) {

        try {
            event.setStatus(OutboxStatus.PROCESSING);
            outboxEventRepository.save(event);

            transactionEventProducer.publish(
                    event.getTopic(),
                    event.getPayload()
            );

            event.setStatus(OutboxStatus.SENT);
            event.setPublishedAt(LocalDateTime.now());
            event.setFailureReason(null);

            outboxEventRepository.save(event);

        } catch (Exception ex) {

            event.setRetryCount(event.getRetryCount() + 1);
            event.setFailureReason(ex.getMessage());

            if (event.getRetryCount() >= MAX_RETRY_COUNT) {
                event.setStatus(OutboxStatus.FAILED);
            } else {
                event.setStatus(OutboxStatus.PENDING);
            }

            outboxEventRepository.save(event);
        }
    }
}