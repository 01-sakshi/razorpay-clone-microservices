package com.merchant_service.security;

import com.merchant_service.cache.ApiKeyCache;
import com.merchant_service.cache.ApiKeyCacheEntry;
import com.merchant_service.entity.ApiKey;
import com.merchant_service.repository.ApiKeyRepository;
import common_lib.exceptions.RateLimitException;
import common_lib.ratelimit.RateLimitResult;
import common_lib.ratelimit.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final String BASIC_PREFIX = "Basic ";
    private final ApiKeyRepository apiKeyRepository;
    private final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();   //To avoid circular dependency with WebSecurityConfig respect to password encoder
    private final MerchantContext merchantContext;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final ApiKeyCache apiKeyCache;  //Since ApiKeyCache has single implementation i.e RedisApiKeyCache so we can directly inject the interface here
    private final RateLimiter rateLimiter;

    @Value("${app.rate-limit.use-case.api-key.requests-per-minute:2}")
    private Integer maxRequestsAllowed;

    /**
     * Passes non-Basic requests onward; for Basic credentials, loads key metadata from Redis or the database,
     * validates the secret and enabled state, rate-limits by key ID, then sets the security and merchant contexts.
     * Invalid credentials and rate-limit failures are delegated to the MVC exception resolver.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authorizationHeader = request.getHeader("Authorization");
            /* We didn't receive api key as part of authorizationHeader in the API */
            if (authorizationHeader == null || !authorizationHeader.startsWith(BASIC_PREFIX)) {
                /* Pass the request to the next security filter since this filter is not applicable for the request */
                filterChain.doFilter(request, response);
                return;
            }

            /* Decode authorizationHeader to get keyId and secretKey */
            String[] decoded = decode(authorizationHeader);
            if (decoded == null) return;
            String secretKey = decoded[1];
            String keyId = decoded[0];

            /* Cache: First try to fetch apiKey entry from redis cache, if not found get it from database and insert in cache */
            ApiKeyCacheEntry apiKeyEntry = apiKeyCache.get(keyId).orElse(null);
            if (apiKeyEntry == null) {
                ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElseThrow(
                        () -> new BadRequestException("Invalid or missing API Key"));
                apiKeyEntry = new ApiKeyCacheEntry(apiKey.getMerchant().getId(),
                        keyId, apiKey.getKeySecretHash(), apiKey.getPreviousKeySecretHash(), apiKey.getEnvironment(),
                        apiKey.getEnabled(), apiKey.getGracePeriodExpiresAt());
                apiKeyCache.put(keyId, apiKeyEntry);
            }
            if (!apiKeyEntry.enabled() || !secretMatches(secretKey, apiKeyEntry))
                throw new BadRequestException("Invalid or missing API Key");

            /* Rate Limiting API requests */
            RateLimitResult rateLimitResult = rateLimiter.check("apiKey:" + keyId, maxRequestsAllowed, 60);
            if (!rateLimitResult.isAllowed()) {
                log.error("Too many requests for keyId: {}", keyId);
                throw new RateLimitException("Too many requests for key - " + keyId, rateLimitResult.retryAfterSeconds());
            }
            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequestsAllowed));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(rateLimitResult.requestsRemaining()));

            /* Set authentication object in SecurityContextHolder */
            var auth = new UsernamePasswordAuthenticationToken(keyId, null,
                    List.of(new SimpleGrantedAuthority("AUTH_KEY_ROLE")));
            SecurityContextHolder.getContext().setAuthentication(auth);
            merchantContext.setMerchantId(apiKeyEntry.merchantId());
            merchantContext.setKeyId(keyId);

            /* Pass the request to the next security filter */
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            /* When we do not specify the handler below and pass it as null, the exception gets propagated
            to GlobalExceptionHandler which in normal cases only handles the exceptions of MVC layer. */
            handlerExceptionResolver.resolveException(request, response, null, e);
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
