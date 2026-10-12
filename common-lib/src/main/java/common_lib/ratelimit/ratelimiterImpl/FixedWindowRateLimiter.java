package common_lib.ratelimit.ratelimiterImpl;

import common_lib.ratelimit.RateLimitResult;
import common_lib.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/*
1. Create a unique Redis key for the API key/client.
2. Increment the request count in Redis.
3. If Redis is unavailable, allow the request.
4. If this is the first request, set the key's TTL = window duration.
5. Check if the request count exceeds the allowed limit.
6. If exceeded → deny the request and return Retry-After based on remaining TTL.
   Otherwise → allow the request and return the remaining request count.
7. Once TTL expires, Redis removes the key and a new window starts. */

@RequiredArgsConstructor
public class FixedWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate apiKeyRedis;

    /**
     * Increments a per-key Redis counter whose TTL starts with the first request in the window.
     * Requests above the limit are denied with the counter's remaining TTL; a null Redis increment fails open.
     *
     * @param key client or credential scope used to isolate the counter
     * @param maxRequestsAllowed maximum count permitted in one window
     * @param windowSeconds fixed-window duration
     * @return whether this request is allowed, remaining requests, or retry delay
     */
    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, int windowSeconds) {
        String redisKey = "rateLimit:fixed:" + key;
        Long count = apiKeyRedis.opsForValue().increment(redisKey);

        if (count == null) return RateLimitResult.allowed(maxRequestsAllowed);   //redis is unavailable
        if (count == 1) apiKeyRedis.expire(redisKey, Duration.ofSeconds(windowSeconds)); //First API hit
        if (count > maxRequestsAllowed) {
            Long ttl = apiKeyRedis.getExpire(redisKey);
            int retryAfterSeconds = ttl != null && ttl > 0 ? ttl.intValue() : windowSeconds;
            return RateLimitResult.denied(retryAfterSeconds);
        }
        return RateLimitResult.allowed((int) (maxRequestsAllowed - count));     //param: no of remaining requests
    }
}
