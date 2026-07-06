package com.example.phoneWallet.services;

import com.example.phoneWallet.Repository.OutboxEventRepository;
import com.example.phoneWallet.entity.OutboxEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(OutboxEventRepository outboxEventRepository,
                              ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(String eventType, String topic, Object payload) {

        try {
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setEventType(eventType);
            outboxEvent.setTopic(topic);
            outboxEvent.setPayload(objectMapper.writeValueAsString(payload));

            outboxEventRepository.save(outboxEvent);

        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize outbox event payload", ex);
        }
    }
}