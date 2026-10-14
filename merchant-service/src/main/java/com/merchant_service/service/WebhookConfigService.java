package com.merchant_service.service;

import com.merchant_service.dto.request.WebhookConfigRequest;
import com.merchant_service.dto.response.WebhookConfigResponse;

import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {

    /**
     * Creates a merchant-owned webhook target, encrypts its generated delivery secret, and returns that secret on creation only.
     */
    WebhookConfigResponse create(UUID merchantId, WebhookConfigRequest webhookConfigRequest);

    /**
     * Lists only configurations owned by the merchant and omits delivery secrets from every response.
     */
    List<WebhookConfigResponse> getAll(UUID merchantId);

    /**
     * Returns a merchant-owned configuration without its secret; absent and foreign IDs are reported as not found.
     */
    WebhookConfigResponse getById(UUID merchantId, UUID webhookConfigId);

    /**
     * Replaces the target URL and event subscriptions of a merchant-owned configuration without changing its secret.
     */
    WebhookConfigResponse update(UUID merchantId, UUID merchantConfigId, WebhookConfigRequest webhookConfigRequest);

    /**
     * Deletes by both merchant and configuration ID so another merchant's configuration cannot be addressed.
     */
    void delete(UUID merchantId, UUID webhookConfigId);
}
