package com.operations_service.webhook;

import java.util.Map;
import java.util.UUID;

import com.operations_service.entity.DlqEvent;
import com.operations_service.entity.WebhookEvent;
import com.operations_service.repository.DlqEventRepository;
import com.operations_service.repository.WebhookEventRepository;
import common_lib.enums.WebhookEventStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookDlqRecorder {

    private final WebhookEventRepository webhookEventRepository;
    private final DlqEventRepository dlqEventRepository;

    /** Uses a new transaction to mark an exhausted delivery {@code DEAD} and persist its final error and payload. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordInDlqAfterRetryAttemptsExhausted(WebhookEvent event, String error) {
        log.info("Recording the DLQ event with event id : {}", event.getId());
        event.setEventStatus(WebhookEventStatus.DEAD);
        webhookEventRepository.save(event);

        DlqEvent dlqEvent = DlqEvent.builder()
                .webhookEvent(event)
                .merchantId(event.getMerchantId())
                .payload(event.getPayload())
                .finalError(error)
                // .movedAt(Instant.now())
                .build();
        dlqEvent = dlqEventRepository.save(dlqEvent);
    }

    /** Persists a failed Kafka envelope as a DLQ row, extracting a UUID merchant ID from {@code data} when possible. */
    public void recordInDlqAfterFailedInConsumer(ConsumerRecord<String, Map<String, Object>> consumerRecord,
            String error) {
        Map<String, Object> envelope = consumerRecord.value();
        UUID merchantId = null;
        try {
            Map<String, Object> payload = (Map<String, Object>) envelope.get("data");
            Object merchantIdRaw = payload.get("merchantId");
            if (merchantIdRaw != null) {
                merchantId = UUID.fromString(merchantIdRaw.toString());
            }
        } catch (Exception ignored) {

        }
        DlqEvent dlqEvent = DlqEvent.builder()
                .webhookEvent(null)
                .finalError(error)
                .payload(envelope != null ? envelope : Map.of())
                .merchantId(merchantId)
                .build();
        dlqEvent = dlqEventRepository.save(dlqEvent);
    }
}
