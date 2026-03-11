package com.apiframework.testng.retry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class RetryReportingListener implements ITestListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RetryReportingListener.class);

    private final Set<String> passedAfterRetryTests = ConcurrentHashMap.newKeySet();

    @Override
    public void onTestSuccess(ITestResult result) {
        Object attempts = result.getAttribute(FrameworkRetryAnalyzer.RETRY_ATTEMPT_ATTRIBUTE);
        if (attempts instanceof Integer retryCount && retryCount > 0) {
            String testId = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
            passedAfterRetryTests.add(testId);
            LOGGER.warn("Test passed after retry: {} (retry count: {})", testId, retryCount);
            Reporter.log("[framework][retry] retried=true retryCount=" + retryCount + " retryReason=" + result.getAttribute(FrameworkRetryAnalyzer.RETRY_REASON_ATTRIBUTE), true);
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (passedAfterRetryTests.isEmpty()) {
            LOGGER.info("Retry report: no tests passed only after retry.");
        } else {
            LOGGER.warn("Retry report: {} tests passed only after retry", passedAfterRetryTests.size());
            for (String test : passedAfterRetryTests) {
                LOGGER.warn("  - {}", test);
            }
        }

        writeMetrics(context);
    }

    private void writeMetrics(ITestContext context) {
        String pathValue = System.getProperty("framework.retry.report.path", "build/reports/retry-metrics.json");
        Path reportPath = Path.of(pathValue);

        try {
            if (reportPath.getParent() != null) {
                Files.createDirectories(reportPath.getParent());
            }
            String json = "{\n"
                + "  \"suite\": \"" + context.getSuite().getName() + "\",\n"
                + "  \"passed\": " + context.getPassedTests().size() + ",\n"
                + "  \"failed\": " + context.getFailedTests().size() + ",\n"
                + "  \"skipped\": " + context.getSkippedTests().size() + ",\n"
                + "  \"passedAfterRetryCount\": " + passedAfterRetryTests.size() + "\n"
                + "}";
            Files.writeString(reportPath, json, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            LOGGER.warn("Failed to write retry metrics report", exception);
        }
    }
}
