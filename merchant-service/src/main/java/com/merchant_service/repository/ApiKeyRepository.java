package com.merchant_service.repository;

import com.merchant_service.entity.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    /** Lists all keys for the merchant, including disabled keys; callers decide how to present their status. */
    List<ApiKey> findByMerchantId(UUID merchantId);
    /** Finds a key only when both its public key ID and owning merchant ID match. */
    Optional<ApiKey> findApiKeyByKeyIdAndMerchantId(String keyId, UUID merchantId);

    /** Resolves the globally unique public key ID for API-key authentication, without a merchant predicate. */
    Optional<ApiKey> findByKeyId(String keyId);
}
