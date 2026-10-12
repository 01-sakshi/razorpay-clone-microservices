package common_lib.exceptions;

import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException {
    private final Integer retryAfterSeconds;
    private final Integer requestsRemaining;

    public RateLimitException(String message, Integer retryAfterSeconds) {
        super(message);
        this.requestsRemaining = 0;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
