package com.merchant_service.cache;

import java.util.Optional;

public interface ApiKeyCache {

    /** Looks up cached authentication metadata by key ID; absence is a cache miss, not proof that the key is invalid. */
    public Optional<ApiKeyCacheEntry> get(String keyId);

    /** Associates the key ID with authentication metadata for subsequent API-key checks. */
    public void put(String KeyId, ApiKeyCacheEntry apiKeyCacheEntry);

    /** Invalidates cached authentication metadata after key revocation or rotation. */
    public void evict(String keyId);
}
