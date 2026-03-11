package com.apiframework.testng.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import java.lang.reflect.Method;
import java.util.concurrent.locks.LockSupport;

public final class FrameworkRetryAnalyzer implements IRetryAnalyzer {
    public static final String RETRY_ATTEMPT_ATTRIBUTE = "framework.retry.attempt";
    public static final String RETRIED_ATTRIBUTE = "framework.retry.retried";
    public static final String RETRY_REASON_ATTRIBUTE = "framework.retry.reason";

    private static final Logger LOGGER = LoggerFactory.getLogger(FrameworkRetryAnalyzer.class);

    private int failedAttemptCount;

    @Override
    public boolean retry(ITestResult result) {
        RetryRuntimePolicy policy = resolvePolicy(result);
        Throwable failure = result.getThrowable();
        RetryPredicate retryPredicate = RetryConfiguration.retryPredicate();

        if (!retryPredicate.shouldRetry(result, failure)) {
            result.setAttribute(RETRIED_ATTRIBUTE, false);
            result.setAttribute(RETRY_REASON_ATTRIBUTE, "non-retryable failure");
            return false;
        }

        if (failedAttemptCount >= policy.maxRetries()) {
            LOGGER.error("Retry limit reached for test {}. Retries: {}", result.getName(), policy.maxRetries());
            result.setAttribute(RETRIED_ATTRIBUTE, false);
            result.setAttribute(RETRY_REASON_ATTRIBUTE, "retry limit reached");
            return false;
        }

        failedAttemptCount++;
        int nextRunNumber = failedAttemptCount + 1;
        result.setAttribute(RETRY_ATTEMPT_ATTRIBUTE, failedAttemptCount);
        result.setAttribute(RETRIED_ATTRIBUTE, true);
        result.setAttribute(RETRY_REASON_ATTRIBUTE, failure == null ? "unknown" : failure.getClass().getSimpleName());

        LOGGER.warn(
            "Retrying test {}. Attempt {} of {}",
            result.getName(),
            nextRunNumber,
            policy.maxAttempts()
        );

        long delayMs = RetryConfiguration.retryDelayStrategy().delayBeforeNextAttemptMs(failedAttemptCount, policy);
        if (delayMs > 0) {
            LockSupport.parkNanos(delayMs * 1_000_000L);
        }
        return true;
    }

    private RetryRuntimePolicy resolvePolicy(ITestResult result) {
        RetryRuntimePolicy global = RetryConfiguration.globalPolicy();

        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        if (method != null) {
            RetrySetting methodPolicy = method.getAnnotation(RetrySetting.class);
            if (methodPolicy != null) {
                return merge(global, methodPolicy);
            }
        }

        RetrySetting classPolicy = result.getTestClass().getRealClass().getAnnotation(RetrySetting.class);
        if (classPolicy != null) {
            return merge(global, classPolicy);
        }

        return global;
    }

    @SuppressWarnings("deprecation")
    private RetryRuntimePolicy merge(RetryRuntimePolicy global, RetrySetting override) {
        int configuredMaxRetries = override.maxRetries() >= 0 ? override.maxRetries() : -1;
        if (configuredMaxRetries < 0 && override.maxAttempts() > 0) {
            configuredMaxRetries = override.maxAttempts() - 1;
        }
        int maxRetries = configuredMaxRetries >= 0 ? configuredMaxRetries : global.maxRetries();
        long delay = override.delayMs() >= 0 ? override.delayMs() : global.baseDelayMs();
        return new RetryRuntimePolicy(maxRetries, delay);
    }
}
