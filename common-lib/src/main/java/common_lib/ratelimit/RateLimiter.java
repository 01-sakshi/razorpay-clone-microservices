package common_lib.ratelimit;

public interface RateLimiter {

    /** Evaluates one request against the selected rate-limit algorithm and returns its allowance or retry delay. */
    RateLimitResult check(String key, int maxRequestsAllowed, int windowSeconds);
}
