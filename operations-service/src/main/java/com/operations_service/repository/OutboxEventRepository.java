package com.operations_service.repository;

import com.operations_service.entity.OutboxEvent;
import common_lib.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    /** Returns all events in the requested status oldest-first so the poller preserves enqueue order. */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus outboxStatus);
}
