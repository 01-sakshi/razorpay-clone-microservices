package common_lib.ratelimit.ratelimiterImpl;

import common_lib.ratelimit.RateLimitResult;
import common_lib.ratelimit.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

// Detail Flow: https://claude.ai/chat/de04c0f3-f94f-44db-844b-98f18c83c2ea
@Slf4j
@RequiredArgsConstructor
public class TokenBucketRateLimiter implements RateLimiter {

    private final StringRedisTemplate redis;

    private static final RedisScript<List> SCRIPT = new DefaultRedisScript<>("""
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local refillPerSec = tonumber(ARGV[2])
            local nowMs = tonumber(ARGV[3])
            local ttlSeconds = tonumber(ARGV[4])

            local data = redis.call('HMGET', key, 'tokens', 'ts')
            local tokens = tonumber(data[1])
            local lastTs = tonumber(data[2])

            if tokens == nil then
                tokens = capacity
                lastTs = nowMs
            end

            local elapsedSec = math.max(0, (nowMs - lastTs) / 1000)
            tokens = math.min(capacity, tokens + elapsedSec * refillPerSec)

            local allowed = 0
            if tokens >= 1 then
                tokens = tokens - 1
                allowed = 1
            end

            redis.call('HMSET', key, 'tokens', tokens, 'ts', nowMs)
            redis.call('EXPIRE', key, ttlSeconds)

            local retryAfter = 0
            if allowed == 0 then
                retryAfter = math.ceil((1 - tokens) / refillPerSec)
            end

            return {allowed, math.floor(tokens), retryAfter}
            """, List.class);

    /**
     * Atomically refills a Redis token bucket at {@code maxRequestsAllowed / windowSeconds} tokens per second,
     * consumes one token when available, and computes the wait for the next token when empty. Redis failures fail open.
     *
     * @param key client or credential scope used to isolate bucket state
     * @param maxRequestsAllowed bucket capacity and request allowance per window
     * @param windowSeconds interval used to derive the refill rate
     * @return whether a token was consumed, remaining whole tokens, or retry delay
     */
    @Override
    public RateLimitResult check(String key, int maxRequestsAllowed, int windowSeconds) {
        try {
            String redisKey = "ratelimit:bucket:" + key;
            double refillPerSec = (double) maxRequestsAllowed / windowSeconds;
            // keep bucket state around for two windows of inactivity before Redis reclaims it
            long ttlSeconds = windowSeconds * 2;

            List<Long> result = redis.execute(SCRIPT,
                    List.of(redisKey),
                    String.valueOf(maxRequestsAllowed),
                    String.valueOf(refillPerSec),
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(ttlSeconds));

            boolean allowed = result.get(0) == 1L;
            int remaining = result.get(1).intValue();
            int retryAfter = result.get(2).intValue();

            return allowed ? RateLimitResult.allowed(remaining) : RateLimitResult.denied(Math.max(1, retryAfter));
        } catch (DataAccessException e) {
            log.warn("Rate limiter unavailable, failing open for key={}", key, e);
            return RateLimitResult.allowed(maxRequestsAllowed);
        }
    }
}
