package com.operations_service.webhook;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookRetryQueue {

    @Value("${app.webhook.delivery.redis-key:webhook-retry}")
    private String key;

    private final StringRedisTemplate redis;

    /**
     * Adds or reschedules the event ID in a Redis sorted set using its retry instant as the score.
     *
     * @param webhookEventId webhook event identifier
     * @param retryAt next delivery time
     */
    public void enqueue(UUID webhookEventId, Instant retryAt) {
        long time = retryAt.toEpochMilli();
        redis.opsForZSet().add(key, webhookEventId.toString(), time);
        log.info("Enqueued a webhook event with id: {}", webhookEventId);
    }

    /**
     * Adds the event ID only if absent, preserving an existing queue score during database reconciliation.
     *
     * @param webhookEventId webhook event identifier
     * @param retryAt next delivery time
     */
    public void enqueueIfAbsent(UUID webhookEventId, Instant retryAt) {
        long time = retryAt.toEpochMilli();
        redis.opsForZSet().addIfAbsent(key, webhookEventId.toString(), time);
        log.info("Enqueued a webhook event with id: {}", webhookEventId);
    }

    /**
     * Removes up to {@code limit} IDs scored no later than now, then returns them for asynchronous delivery.
     *
     * @param limit maximum number of event IDs to claim
     * @return due event identifiers, possibly empty
     */
    public Set<UUID> pollDue(int limit) {
        long time = Instant.now().toEpochMilli();
        // rangeByScoreWithScores is to fetch records on the basis of their
        // score(timestamp)
        // whereas rangeByScore fetches records on the basis of their index.
        Set<TypedTuple<String>> due = redis.opsForZSet().rangeByScoreWithScores(key, 0,
                time, 0, limit);
        if (due == null || due.isEmpty()) {
            return Set.of();
        }
        due.forEach(tuple -> redis.opsForZSet().remove(key, tuple.getValue()));
        // return webhookEventId for all the applicable webhook events
        return due.stream().map(d -> UUID.fromString(d.getValue())).collect(Collectors.toSet());
    }
}
