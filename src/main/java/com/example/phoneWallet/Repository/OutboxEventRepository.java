package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.OutboxEvent;
import com.example.phoneWallet.enums.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}