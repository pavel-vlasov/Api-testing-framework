package com.apiframework.testng.retry;

import org.testng.ITestResult;

@FunctionalInterface
public interface RetryPredicate {
    boolean shouldRetry(ITestResult result, Throwable failure);
}
