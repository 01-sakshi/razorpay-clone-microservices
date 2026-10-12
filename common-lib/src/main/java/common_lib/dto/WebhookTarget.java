package com.payment_gateway.razorpay.common.dto;

import java.util.UUID;

/**
 * Describes an enabled merchant webhook destination and its signing material.
 *
 * @param configId webhook configuration identifier
 * @param targetUrl destination URL
 * @param webhookSecret signing secret; sensitive and not intended for API responses or logs
 */
public record WebhookTarget(
        UUID configId,
        String targetUrl,
        String webhookSecret) {

}
