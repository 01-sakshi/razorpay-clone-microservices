package common_lib.cache;

import common_lib.enums.Environment;

import java.time.Instant;
import java.util.UUID;

/**
 * Cached API-key verification data, including the optional rotation grace-period hash.
 *
 * @param merchantId owning merchant
 * @param keyId public key identifier
 * @param keySecretHash current secret hash; raw secrets are never cached here
 * @param previousKeySecretHash previous hash accepted during rotation grace, or {@code null}
 * @param environment credential environment
 * @param enabled whether the key may authenticate requests
 * @param gracePeriodExpiresAt end of the previous-key grace period, or {@code null}
 */
public record ApiKeyCacheEntry(
        UUID merchantId,
        String keyId,
        String keySecretHash,
        String previousKeySecretHash,
        Environment environment,
        Boolean enabled,
        Instant gracePeriodExpiresAt

) {

    /**
     * Returns true only while a rotation grace-period expiry is present and still in the future.
     *
     * @return whether the previous key remains within its grace period
     */
    public Boolean isInGracePeriod() {
        return gracePeriodExpiresAt != null && Instant.now().isBefore(gracePeriodExpiresAt);
    }
}
