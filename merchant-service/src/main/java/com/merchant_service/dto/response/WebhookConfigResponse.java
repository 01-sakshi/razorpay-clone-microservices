package com.merchant_service.dto.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Merchant webhook configuration returned by the API.
 *
 * @param id configuration identifier
 * @param targetUrl delivery URL
 * @param eventTypes configured event names, or the configured all-events representation
 * @param webhookSecret signing secret when the create flow exposes it; omitted from later reads
 * @param enabled whether delivery is active
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebhookConfigResponse(
    UUID id,
    String targetUrl,
    String eventTypes,
    String webhookSecret,
    boolean enabled
) {
    
}
