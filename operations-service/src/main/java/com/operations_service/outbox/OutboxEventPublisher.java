package com.operations_service.outbox;

import com.operations_service.entity.OutboxEvent;
import com.operations_service.repository.OutboxEventRepository;
import common_lib.enums.EventAggregateType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    /** Saves the aggregate event and payload as an outbox row for the scheduled Kafka poller to publish. */
    public void publish(EventAggregateType aggregateType, UUID aggregateId, String eventType,
                        Map<String, Object> payload) {

        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .publishedAt(Instant.now())
                .build();
        outboxEventRepository.save(outboxEvent);
    }
}
