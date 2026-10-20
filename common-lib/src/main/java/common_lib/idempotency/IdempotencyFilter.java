package common_lib.idempotency;

import common_lib.context.MerchantContext;
import common_lib.exceptions.IdempotencyConflictException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {
    private static final Set<String> GUARDED_METHODS = Set.of("POST", "PUT", "PATCH");
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(30);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);
    private static final String SEPARATOR = "|";

    //    Bean of IdempotencyFilter is created in SharedResilienceAutoConfiguration file
    private final MerchantContext merchantContext;
    private final IdempotencyStore idempotencyStore;
    private final HandlerExceptionResolver handlerExceptionResolver;

    /**
     * Guards POST, PUT, and PATCH requests carrying an idempotency key; keys are scoped by merchant when present.
     * Concurrent in-progress reuse conflicts, while completed non-error responses with bodies are replayed for
     * 24 hours; failed or empty responses release the claim so the client can retry.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        if (!GUARDED_METHODS.contains(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String rawKey = request.getHeader("X-Idempotency-Key");
        if (rawKey == null || rawKey.isBlank()) { // No idem-key found, continue
            chain.doFilter(request, response);
            return;
        }

        UUID merchantId = merchantContext.getMerchantId();
        String key = merchantId != null ? merchantId + ":" + rawKey : rawKey;

        boolean claimed = idempotencyStore.setIfAbsent(key, IN_PROGRESS_TTL);

        if (!claimed) {
            // another thread has already claimed this key
            Optional<String> existing = idempotencyStore.get(key);

            if (existing.isPresent() && !IdempotencyStore.IN_PROGRESS.equals(existing.get())) {
                // it's not in-progress, but coming from the actual value stored in redis
                replay(request, response, existing.get());
            } else {
                // it's in progress by another thread
                var ex = new IdempotencyConflictException("A request with this idempotency key is in progress");
                handlerExceptionResolver.resolveException(request, response, null, ex);
            }
            return;
        }

        // first time claim
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(request, wrapper);
        } finally {
            int status = wrapper.getStatus();
            byte[] bodyBytes = wrapper.getContentAsByteArray();
            String body = new String(bodyBytes, StandardCharsets.UTF_8);

            if (status < 400 && bodyBytes.length > 0) {
                // Success — store the completed response for future replays
                String stored = status + SEPARATOR + body;
                idempotencyStore.store(key, stored, COMPLETED_TTL);
                log.debug("IdempotencyFilter: stored response status={} key={}", status, key);
            } else {
                // Error or empty — delete placeholder so client can retry cleanly
                idempotencyStore.delete(key);
                log.debug("IdempotencyFilter: deleted placeholder after error status={} key={}", status, key);
            }
            // Always flush buffered body to the actual response.
            // If this is skipped the client receives an empty body.
            wrapper.copyBodyToResponse();
        }
    }

    /**
     * Restores the saved status and JSON body; malformed stored data is routed through the conflict handler.
     *
     * @param request  request associated with the replay
     * @param response response that receives the saved result
     * @param stored   serialized status and body separated by {@code |}
     */
    private void replay(HttpServletRequest request, HttpServletResponse response, String stored) throws IOException {
        int separatorIndex = stored.indexOf(SEPARATOR);
        if (separatorIndex < 0) {
            var ex = new IdempotencyConflictException("A request with this idempotency key is in progress");
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }

        int status = Integer.parseInt(stored.substring(0, separatorIndex));
        String body = stored.substring(separatorIndex + 1);

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}
