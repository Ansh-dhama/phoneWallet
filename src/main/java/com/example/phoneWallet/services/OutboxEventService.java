package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.OutboxEventRepository;
import com.example.phoneWallet.entity.OutboxEvent;
import com.example.phoneWallet.enums.OutboxStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OutboxEventService {
    private static final int MAX_RETRY_COUNT = 5;
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(String eventType, String topic, Object payload) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setEventType(eventType);
            event.setTopic(topic);
            event.setPayload(objectMapper.writeValueAsString(payload));
            repository.save(event);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize outbox payload", ex);
        }
    }

    /** Claim DB rows in a short transaction. Kafka I/O happens after this commits. */
    @Transactional
    public List<OutboxEvent> claimPendingBatch() {
        List<OutboxEvent> events = repository.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        events.forEach(e -> {
            e.setStatus(OutboxStatus.PROCESSING);
            e.setProcessingStartedAt(now);
        });
        repository.saveAll(events);
        return List.copyOf(events);
    }

    @Transactional
    public void markSent(Long id) {
        OutboxEvent event = repository.findById(id).orElseThrow();
        event.setStatus(OutboxStatus.SENT);
        event.setPublishedAt(LocalDateTime.now());
        event.setProcessingStartedAt(null);
        event.setFailureReason(null);
        repository.save(event);
    }

    @Transactional
    public void markPublishFailure(Long id, String reason) {
        OutboxEvent event = repository.findById(id).orElseThrow();
        event.setRetryCount(event.getRetryCount() + 1);
        event.setFailureReason(reason == null ? "Kafka publish failed" : reason.substring(0, Math.min(reason.length(), 1000)));
        event.setProcessingStartedAt(null);
        event.setStatus(event.getRetryCount() >= MAX_RETRY_COUNT ? OutboxStatus.FAILED : OutboxStatus.PENDING);
        repository.save(event);
    }

    @Transactional
    public int recoverStaleProcessing(LocalDateTime before) {
        List<OutboxEvent> stale = repository.findStaleProcessing(OutboxStatus.PROCESSING, before);
        stale.forEach(e -> {
            e.setStatus(OutboxStatus.PENDING);
            e.setProcessingStartedAt(null);
            e.setFailureReason("Recovered stale PROCESSING event");
        });
        repository.saveAll(stale);
        return stale.size();
    }
}
