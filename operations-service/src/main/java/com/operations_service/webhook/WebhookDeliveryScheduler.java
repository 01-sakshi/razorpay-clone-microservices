package com.operations_service.webhook;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.operations_service.entity.WebhookEvent;
import com.operations_service.repository.WebhookEventRepository;
import common_lib.enums.WebhookEventStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDeliveryScheduler {

    private final WebhookRetryQueue retryQueue;
    private final WebhookDeliverExecutor webhookDeliverExecutor;
    private final WebhookEventRepository webhookEventRepository;
    private ExecutorService virtualExecutorService;

    /** Initializes the per-task virtual-thread executor used by the polling worker. */
    @PostConstruct
    void init() {
        virtualExecutorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    /** Stops accepting webhook delivery tasks as the application component is destroyed. */
    @PreDestroy
    void shutdown() {
        virtualExecutorService.shutdown();
    }

    @Value("${app.webhook.delivery.poll-batch-size:100}")
    private int batchSize;

    /** Polls up to the configured batch size of due IDs every five seconds and submits each delivery independently. */
    @Scheduled(fixedDelay = 5000)
    public void pollAndDeliver() {
        Set<UUID> pollDue = retryQueue.pollDue(batchSize);
        if (pollDue == null || pollDue.isEmpty()) {
            return;
        }
        for (UUID webhookEventId : pollDue) {
            virtualExecutorService.submit(() -> {
                webhookDeliverExecutor.deliver(webhookEventId);
            });
        }
    }

    /** Repairs missed Redis queue entries every ten seconds from due database rows marked {@code PENDING}. */
    @Scheduled(fixedDelay = 10000)
    public void reconcileFromDatabase() {
        List<WebhookEvent> events = webhookEventRepository
                .findByEventStatusAndNextRetryAtBefore(WebhookEventStatus.PENDING, Instant.now());
        events.forEach(event -> retryQueue.enqueueIfAbsent(event.getId(), event.getNextRetryAt()));
    }
}
