package com.operations_service.webhook;

import com.operations_service.client.MerchantServiceClient;
import com.operations_service.entity.WebhookEvent;
import com.operations_service.repository.WebhookEventRepository;
import common_lib.dto.WebhookTarget;
import common_lib.enums.WebhookEventStatus;
import common_lib.util.SignerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
/*
 * Consumes payment, order, refund, and settlement events and schedules merchant webhook deliveries.
 */
public class WebhookKafkaConsumer {

    private final MerchantServiceClient merchantServiceClient;
    private final JsonMapper jsonMapper;
    private final SignerUtil signerUtil;
    private final WebhookEventRepository webhookEventRepository;
    private final WebhookRetryQueue retryQueue;
    private final WebhookDlqRecorder dlqRecorder;

    /**
     * Resolves the event's merchant subscriptions, signs and persists one pending delivery per target, then queues each
     * delivery before acknowledging. Database outages leave the Kafka offset unacknowledged for redelivery; malformed
     * or otherwise unprocessable records are acknowledged and recorded in the dead-letter table.
     */
    @KafkaListener(topics = {
            "${app.kafka.topics.payment:payment.events}",
            "${app.kafka.topics.order:order.events}",
            "${app.kafka.topics.refund:refund.events}",
            "${app.kafka.topics.settlement:settlement.events}"
        }, properties = {
            "spring.json.value.default.type=java.util.HashMap",
            "spring.json.trusted.packages=java.util,java.lang"
        })
    public void listenOnWebhookEvent(ConsumerRecord<String, Map<String, Object>> consumerRecord,
                                     Acknowledgment acknowledgment) {
        try {
            log.info("Consumer Record: {}", consumerRecord);
            Map<String, Object> envelope = consumerRecord.value();
            Map<String, Object> payload = (Map<String, Object>) envelope.get("data");
            String eventType = (String) envelope.get("eventType");
            Object merchantIdRaw = payload.get("merchantId");

            if (merchantIdRaw == null) {
                log.error("No merchantId was found, skipping event: {}", eventType);
                acknowledgment.acknowledge();
                return;
            }

            UUID merchantId = UUID.fromString(merchantIdRaw.toString());
            List<WebhookTarget> activeConfigsForEvent = merchantServiceClient.getActiveConfigsForEvent(merchantId, eventType);
            if (activeConfigsForEvent.isEmpty()) {
                log.info("No webhook target was found, so skipping event : {}", eventType);
                acknowledgment.acknowledge();
                return;
            }

            Map<String, Object> signatureData = Map.of("event", eventType, "payload", payload);
            String signatureJson = jsonMapper.writeValueAsString(signatureData);

            for (WebhookTarget target : activeConfigsForEvent) {
                String signature = signerUtil.sign(signatureJson, target.webhookSecret());

                WebhookEvent webhookEvent = WebhookEvent.builder()
                        .eventStatus(WebhookEventStatus.PENDING)
                        .merchantId(merchantId)
                        // .lastAttemptAt(Instant.now())
                        // .lastResponseBody(signatureJson)
                        .payload(payload)
                        .targetUrl(target.targetUrl())
                        .eventType(eventType)
                        .signature(signature)
                        .nextRetryAt(Instant.now())
                        .build();
                webhookEvent = webhookEventRepository.save(webhookEvent);
                retryQueue.enqueue(webhookEvent.getId(), webhookEvent.getNextRetryAt());
                log.info("Created a webhook event with id: {}", webhookEvent.getId());
            }
            acknowledgment.acknowledge();
        } catch (DataAccessException | CannotCreateTransactionException dbDown) {
            // This is intentional — offset stays where it was, so Kafka will redeliver this
            // same message later once DB is back up
            log.error("Webhook consumer failed due to DB down, failed to process the record, offset: {}",
                    consumerRecord.offset(), dbDown);
        } catch (Exception logicError) {
            log.error("Webhook consumer failed to process the record, offset: {}", consumerRecord.offset(), logicError);
            acknowledgment.acknowledge();
            dlqRecorder.recordInDlqAfterFailedInConsumer(consumerRecord, logicError.getMessage());
        }
    }
}
