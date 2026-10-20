package com.api_gateway.security;

import com.api_gateway.client.ApiKeyLookupClient;
import common_lib.cache.ApiKeyCache;
import common_lib.cache.ApiKeyCacheEntry;
import common_lib.exceptions.GatewayAuthenticationException;
import common_lib.exceptions.RateLimitException;
import common_lib.ratelimit.RateLimitResult;
import common_lib.ratelimit.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthHandler {

    private final String BASIC_PREFIX = "Basic ";

    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();
    private final ApiKeyCache apiKeyCache;  //Since ApiKeyCache has single implementation i.e RedisApiKeyCache so we can directly inject the interface here
    private final RateLimiter rateLimiter;
    private final ApiKeyLookupClient apiKeyLookupClient;

    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:2}")
    private Integer maxRequestsAllowed;

    public Map<String, String> authenticate(String authorizationHeader, HttpServletResponse response) throws BadRequestException {
        String[] decoded = decode(authorizationHeader);
        if (decoded == null) throw new GatewayAuthenticationException("API_KEY_AUTH_FAILED",
                null, null, "Malformed Api Key passed");
        String keyId = decoded[0];
        String secretKey = decoded[1];

        /* Cache: First try to fetch apiKey entry from redis cache, if not found get it from database and insert in cache */
        ApiKeyCacheEntry apiKeyEntry = apiKeyCache.get(keyId).orElse(loadAndCache(keyId));
        if (apiKeyEntry == null || !apiKeyEntry.enabled() || !secretMatches(secretKey, apiKeyEntry))
            throw new BadRequestException("Invalid or missing API Key");

        /* Rate Limiting API requests */
        RateLimitResult rateLimitResult = rateLimiter.check("apiKey:" + keyId, maxRequestsAllowed, 60);
        if (!rateLimitResult.isAllowed()) {
            log.error("Too many requests for keyId: {}", keyId);
            throw new RateLimitException("Too many requests for key - " + keyId, rateLimitResult.retryAfterSeconds());
        }
        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsAllowed));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.requestsRemaining()));

        return Map.of(
                "X-Merchant-Id", apiKeyEntry.merchantId().toString(),
                "X-Environment", apiKeyEntry.environment().name(),
                "X-Key-Id", apiKeyEntry.keyId()
        );
    }

    private ApiKeyCacheEntry loadAndCache(String keyId) {
        try {
            ApiKeyCacheEntry cacheEntry = apiKeyLookupClient.findByKeyId(keyId);    //Directly get cache entry DTO from apiKeyLookupClient
            apiKeyCache.put(keyId, cacheEntry);
            return cacheEntry;
        } catch (Exception e) {
            log.warn("No Api Key found for keyId: {}", keyId);
            return null;
        }
    }

    /**
     * Accepts a BCrypt match against the current secret hash, otherwise evaluates the cached grace-period fallback.
     * The fallback comparison is performed against the entry's stored key fields as implemented here.
     *
     * @param rawSecret   secret supplied in the Basic credentials
     * @param apiKeyEntry cached hashes and rotation-grace metadata for the key
     * @return whether the credential passes the current or grace-period check
     */
    private boolean secretMatches(String rawSecret, ApiKeyCacheEntry apiKeyEntry) {
        if (BCRYPT.matches(rawSecret, apiKeyEntry.keySecretHash()))
            return true;   /* Verify the encoded password obtained from storage matches the submitted raw
         password after it too is encoded. */
        return apiKeyEntry.isInGracePeriod() &&
                apiKeyEntry.keySecretHash() != null &&
                BCRYPT.matches(rawSecret, apiKeyEntry.previousKeySecretHash());
    }

    /**
     * Base64-decodes the text after {@code Basic } and splits at the first colon, preserving colons in the secret.
     *
     * @param header Authorization header with the Basic scheme
     * @return key ID and secret in that order, or {@code null} when the decoded value has no colon
     */
    private String[] decode(String header) {
        String encoded = header.substring(BASIC_PREFIX.length());
        String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        int colon = decoded.indexOf(':');
        if (colon < 0) return null;
        return new String[]{decoded.substring(0, colon), decoded.substring(colon + 1)};
    }
}
