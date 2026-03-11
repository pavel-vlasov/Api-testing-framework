package com.apiframework.testng.retry;

import org.testng.ITestResult;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

public final class DefaultRetryPredicate implements RetryPredicate {
    @Override
    public boolean shouldRetry(ITestResult result, Throwable failure) {
        if (failure == null) {
            return false;
        }
        if (failure instanceof AssertionError) {
            return false;
        }
        if (failure instanceof ConnectException || failure instanceof SocketTimeoutException || failure instanceof TimeoutException) {
            return true;
        }

        String message = failure.getMessage() == null ? "" : failure.getMessage().toLowerCase();
        return message.contains("timeout") || message.contains("connection reset") || message.contains("502")
            || message.contains("503") || message.contains("504");
    }
}
