package com.apiframework.testng.retry;

public final class FixedRetryDelayStrategy implements RetryDelayStrategy {
    @Override
    public long delayBeforeNextAttemptMs(int retryAttempt, RetryRuntimePolicy policy) {
        return policy.baseDelayMs();
    }
}
