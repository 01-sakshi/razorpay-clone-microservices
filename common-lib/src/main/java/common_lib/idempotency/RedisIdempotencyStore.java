package common_lib.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@RequiredArgsConstructor    //RequiredArgsConstructor creates a constructor only for final fields and fields marked as @NonNull
@Slf4j
public class RedisIdempotencyStore implements IdempotencyStore {

    private final StringRedisTemplate redis;
    String prefix = "idempotency:";

    /**
     * Writes the serialized response to Redis with the requested TTL; Redis write failures are logged, not propagated.
     *
     * @param key idempotency key
     * @param value serialized response
     * @param ttl retention period
     */
    @Override
    public void store(String key, String value, Duration ttl) {
        try {
            redis.opsForValue().set(prefix + key, value, ttl);
        } catch (DataAccessException e) {
            log.error("Failed to store key: {}", key, e);
        }
    }

    /**
     * Uses Redis set-if-absent to claim a key; on Redis failure this implementation fails open and permits processing.
     *
     * @param key idempotency key
     * @param ttl claim retention period
     * @return {@code true} when the key was claimed or Redis was unavailable, otherwise {@code false}
     */
    @Override
    public boolean setIfAbsent(String key, Duration ttl) {
        try {
            return redis.opsForValue().setIfAbsent(prefix + key, IN_PROGRESS, ttl);
        } catch (DataAccessException e) {
            log.error("Idempotency Store unavailable, failing open for key: {}", key, e);
            return true;
        }
    }

    /**
     * Deletes the namespaced Redis key; a Redis deletion failure is logged and swallowed.
     *
     * @param key idempotency key
     */
    @Override
    public void delete(String key) {
        try {
            redis.delete(prefix + key);
        } catch (DataAccessException e) {
            log.error("Failed to remove key from redis, key: {}", key, e);
        }
    }

    /**
     * Reads the namespaced Redis value, returning empty both when absent and when Redis cannot be read.
     *
     * @param key idempotency key
     * @return stored value, or empty when absent or unavailable
     */
    @Override
    public Optional<String> get(String key) {
        try {
            return Optional.ofNullable(redis.opsForValue().get(prefix + key));
        } catch (DataAccessException e) {
            log.error("Failed to fetch key from redis, key: {}", key, e);
            return Optional.empty();
        }
    }
}
