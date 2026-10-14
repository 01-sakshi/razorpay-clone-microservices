package com.operations_service.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.operations_service.entity.WebhookEvent;
import common_lib.enums.WebhookEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {

    /** Selects events with the exact status and a retry timestamp strictly earlier than the cutoff for queue reconciliation. */
    List<WebhookEvent> findByEventStatusAndNextRetryAtBefore(WebhookEventStatus pending, Instant instant);

}
