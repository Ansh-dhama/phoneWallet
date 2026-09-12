package com.example.phoneWallet.Repository;

import com.example.phoneWallet.entity.OutboxEvent;
import com.example.phoneWallet.enums.OutboxStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Query("select e from OutboxEvent e where e.status = :status and e.processingStartedAt < :before")
    List<OutboxEvent> findStaleProcessing(
            @Param("status") OutboxStatus status,
            @Param("before") LocalDateTime before
    );
}
