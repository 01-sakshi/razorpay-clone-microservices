package com.merchant_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request to create or update a merchant webhook configuration.
 *
 * @param targetUrl  required HTTP or HTTPS delivery URL, up to 500 characters
 * @param eventTypes comma-separated event names, {@code null}, blank, or {@code ALL} for every event
 */
public record WebhookConfigRequest(
        @NotBlank(message = "targetUrl cannot be blank")
        @Size(max = 500)
        @Pattern(regexp = "^(https?)://.*$", message = "targetUrl must be a valid URL starting with http or https")
        String targetUrl,

        // Comma-separated fine-grained event type names (e.g. "PAYMENT_STATUS_CHANGED, REFUND_CREATED").
        // Null/blank/"ALL" subscribes to every event type.
        @Size(max = 1000)
        String eventTypes
) {

}
