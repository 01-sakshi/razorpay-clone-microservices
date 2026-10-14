package com.merchant_service.controller;

import com.merchant_service.dto.request.WebhookConfigRequest;
import com.merchant_service.dto.response.WebhookConfigResponse;
import com.merchant_service.service.WebhookConfigService;
import common_lib.context.MerchantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Authenticated merchant endpoints for webhook destination configuration.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/merchants/webhooks")
public class WebhookConfigController {

    private final MerchantContext merchantContext;
    private final WebhookConfigService webhookConfigService;

    /**
     * Registers a webhook target through {@code POST /v1/merchants/webhooks/create} and event subscriptions for the authenticated merchant. The generated signing secret
     * is returned by the create response and is not exposed by later read operations.
     *
     * @param webhookConfigRequest validated target URL, signing secret inputs, and subscribed events
     * @return HTTP 200 with the created configuration and its one-time secret
     */
    @PostMapping(path = "/create")
    public ResponseEntity<WebhookConfigResponse> create(@Valid @RequestBody WebhookConfigRequest webhookConfigRequest) {
        return ResponseEntity.ok(webhookConfigService.create(merchantContext.getMerchantId(), webhookConfigRequest));
    }

    /**
     * Lists webhook targets through {@code GET /v1/merchants/webhooks} configured by the authenticated merchant; configurations belonging to other merchants
     * are excluded, and delivery secrets are not returned.
     *
     * @return HTTP 200 with the merchant's webhook configurations
     */
    @GetMapping
    public ResponseEntity<List<WebhookConfigResponse>> getAll() {
        return ResponseEntity.ok(webhookConfigService.getAll(merchantContext.getMerchantId()));
    }

    /**
     * Retrieves a configuration through {@code GET /v1/merchants/webhooks/{configId}} only when it belongs to the authenticated merchant. A missing or foreign ID is
     * reported as not found, preventing disclosure of another merchant's configuration.
     *
     * @param configId identifier of the configuration to retrieve
     * @return HTTP 200 with the configuration, excluding its delivery secret
     */
    @GetMapping("/{configId}")
    public ResponseEntity<WebhookConfigResponse> getById(@PathVariable UUID configId) {
        return ResponseEntity.ok(webhookConfigService.getById(merchantContext.getMerchantId(), configId));
    }

    /**
     * Validates and replaces the target URL and event subscriptions through {@code PUT /v1/merchants/webhooks/{configId}} of a configuration owned by the authenticated
     * merchant; the service rejects IDs that are missing or owned elsewhere.
     *
     * @param configId             identifier of the configuration to update
     * @param webhookConfigRequest validated target URL and replacement event subscriptions
     * @return HTTP 200 with the updated configuration
     */
    @PutMapping("/{configId}")
    public ResponseEntity<WebhookConfigResponse> update(@PathVariable UUID configId,
                                                        @Valid @RequestBody WebhookConfigRequest webhookConfigRequest) {
        return ResponseEntity
                .ok(webhookConfigService.update(merchantContext.getMerchantId(), configId, webhookConfigRequest));
    }

    /**
     * Deletes a webhook configuration through {@code DELETE /v1/merchants/webhooks/{configId}} within the authenticated merchant's scope. Successful deletion returns no
     * representation in the response body.
     *
     * @param configId identifier of the configuration to delete
     * @return HTTP 204 when deletion succeeds
     */
    @DeleteMapping("/{configId}")
    public ResponseEntity<Void> delete(@PathVariable UUID configId) {
        webhookConfigService.delete(merchantContext.getMerchantId(), configId);
        return ResponseEntity.noContent().build();
    }
}
