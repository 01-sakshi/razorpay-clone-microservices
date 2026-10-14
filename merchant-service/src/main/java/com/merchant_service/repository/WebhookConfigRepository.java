package com.merchant_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.merchant_service.entity.MerchantWebhookConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookConfigRepository extends JpaRepository<MerchantWebhookConfig, UUID> {

    /** Lists every configuration for the merchant, including disabled targets. */
    List<MerchantWebhookConfig> findByMerchantId(UUID merchantId);

    /** Resolves a configuration by the composite merchant/configuration scope; foreign IDs produce an empty result. */
    Optional<MerchantWebhookConfig> findByMerchantIdAndId(UUID merchantId, UUID webhookConfigId);

    /** Deletes rows matching both IDs; a missing or foreign configuration results in no matching deletion. */
    void deleteByMerchantIdAndId(UUID merchantId, UUID webhookConfigId);

    /** Lists only enabled delivery targets in the merchant's scope for event subscription filtering. */
    List<MerchantWebhookConfig> findByMerchantIdAndEnabledTrue(UUID merchantId);
    
}
