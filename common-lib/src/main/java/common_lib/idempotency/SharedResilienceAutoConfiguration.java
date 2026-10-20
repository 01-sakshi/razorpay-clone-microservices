package common_lib.idempotency;

import common_lib.cache.RedisApiKeyCache;
import common_lib.context.MerchantContext;
import common_lib.ratelimit.RateLimiter;
import common_lib.ratelimit.ratelimiterImpl.FixedWindowRateLimiter;
import common_lib.ratelimit.ratelimiterImpl.SlidingWindowLuaScripter;
import common_lib.ratelimit.ratelimiterImpl.SlidingWindowRateLimiter;
import common_lib.ratelimit.ratelimiterImpl.TokenBucketRateLimiter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
public class SharedResilienceAutoConfiguration {

    @Bean
    public RedisApiKeyCache redisApiKeyCache(StringRedisTemplate stringRedisTemplate,
                                             ObjectMapper objectMapper) {
        return new RedisApiKeyCache(stringRedisTemplate, objectMapper);
    }

    /* All the beans dependent on StringRedisTemplate are put in this config file */

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory); //to establish redis connection
    }

    @Bean
    public IdempotencyStore idempotencyStore(StringRedisTemplate stringRedisTemplate) {
        return new RedisIdempotencyStore(stringRedisTemplate);
    }

    @Bean
    public IdempotencyFilter idempotencyFilter(MerchantContext merchantContext,
                                               IdempotencyStore idempotencyStore,
                                               HandlerExceptionResolver handlerExceptionResolver) {
        return new IdempotencyFilter(merchantContext, idempotencyStore, handlerExceptionResolver);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "fixed")
    public RateLimiter fixedWindowRateLimiter(StringRedisTemplate stringRedisTemplate) {
        return new FixedWindowRateLimiter(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding-lua")
    public RateLimiter slidingWindowLuaScripter(StringRedisTemplate stringRedisTemplate) {
        return new SlidingWindowLuaScripter(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "sliding")
    public RateLimiter slidingWindowRateLimiter(StringRedisTemplate stringRedisTemplate) {
        return new SlidingWindowRateLimiter(stringRedisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.method", havingValue = "bucket")
    public RateLimiter tokenBucketRateLimiter(StringRedisTemplate stringRedisTemplate) {
        return new TokenBucketRateLimiter(stringRedisTemplate);
    }
}
