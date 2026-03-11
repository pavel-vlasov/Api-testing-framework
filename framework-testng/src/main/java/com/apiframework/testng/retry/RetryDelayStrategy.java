package com.apiframework.testng.retry;

@FunctionalInterface
public interface RetryDelayStrategy {
    long delayBeforeNextAttemptMs(int retryAttempt, RetryRuntimePolicy policy);
}
