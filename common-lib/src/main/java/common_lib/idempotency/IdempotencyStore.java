package common_lib.idempotency;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyStore {

    String IN_PROGRESS = "__IN_PROGRESS__";

    /** Stores the completed response under the key until the supplied time-to-live expires. */
    void store(String key, String value, Duration ttl);

    /** Atomically reserves an absent key for an in-progress request, applying the reservation lifetime. */
    boolean setIfAbsent(String key, Duration ttl);

    /** Deletes the key so an unsuccessful request can be retried or its state discarded. */
    void delete(String key);

    /** Reads either the in-progress marker or the completed response currently associated with the key. */
    Optional<String> get(String key);
}
