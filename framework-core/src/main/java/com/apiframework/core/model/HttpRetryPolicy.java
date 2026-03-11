package com.apiframework.core.model;

import java.time.Duration;
import java.util.Set;

public record HttpRetryPolicy(int maxAttempts, Duration delayBetweenAttempts, Set<Integer> retryableStatusCodes) {

    public HttpRetryPolicy {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        if (delayBetweenAttempts.isNegative()) {
            throw new IllegalArgumentException("delayBetweenAttempts must be >= 0");
        }
        retryableStatusCodes = Set.copyOf(retryableStatusCodes);
    }

    public static HttpRetryPolicy defaults() {
        return new HttpRetryPolicy(1, Duration.ZERO, Set.of(429, 500, 502, 503, 504));
    }
}
