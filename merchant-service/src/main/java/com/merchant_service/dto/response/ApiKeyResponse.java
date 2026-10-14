package com.merchant_service.dto.response;

import common_lib.enums.Environment;

import java.time.Instant;
import java.util.UUID;

/**
 * Non-secret API-key metadata returned after creation.
 *
 * @param id credential identifier
 * @param keyId public key identifier
 * @param environment credential environment
 * @param enabled whether the key is active
 * @param lastUsedAt last successful use, or {@code null}
 * @param createdAt creation time
 */
public record ApiKeyResponse(
        UUID id,
        String keyId,
        Environment environment,
        boolean enabled,
        Instant lastUsedAt,
        Instant createdAt

) {
}
