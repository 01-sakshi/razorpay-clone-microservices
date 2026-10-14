package com.merchant_service.service;

import com.merchant_service.dto.request.CreateApiKeyRequest;
import com.merchant_service.dto.response.ApiKeyCreateResponse;
import com.merchant_service.dto.response.ApiKeyResponse;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    /**
     * Creates merchant-scoped credentials for the requested environment; only the returned create response contains the raw secret.
     *
     * @param merchantId owner of the new key
     * @param createApiKeyRequest environment for which credentials are issued
     * @return generated key ID and one-time raw secret
     */
    ApiKeyCreateResponse create(UUID merchantId, CreateApiKeyRequest createApiKeyRequest);

    /** Returns this merchant's key metadata without exposing secret material. */
    List<ApiKeyResponse> list(UUID merchantId);

    /** Disables a key only when it belongs to the merchant and evicts cached credentials; missing keys fail as not found. */
    String revoke(UUID merchantId, String keyId);

    /** Rotates a merchant-owned enabled key, retains the prior hash for the configured grace interval, and returns replacement credentials. */
    ApiKeyCreateResponse rotate(UUID merchantId, String keyId);
}
