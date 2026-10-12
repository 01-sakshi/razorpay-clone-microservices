package common_lib.ratelimit;

/**
 * Reports the result and retry metadata of a rate-limit decision.
 *
 * @param isAllowed whether the request may proceed
 * @param requestsRemaining requests available after the decision
 * @param retryAfterSeconds delay before retrying a denied request
 */
public record RateLimitResult(
        boolean isAllowed,
        int requestsRemaining,
        int retryAfterSeconds
) {

    /**
     * Creates an allowed decision with the caller's post-request allowance and no retry delay.
     *
     * @param requestsRemaining requests remaining after the decision
     * @return an allowed result
     */
    public static RateLimitResult allowed(int requestsRemaining) {
        return new RateLimitResult(true, requestsRemaining, 0);
    }

    /**
     * Creates a denied decision with zero remaining requests and the delay before retrying.
     *
     * @param retryAfterSeconds delay before retrying
     * @return a denied result
     */
    public static RateLimitResult denied(int retryAfterSeconds) {
        return new RateLimitResult(false, 0, retryAfterSeconds);
    }
}
