package com.apiframework.reporting.allure;

import com.apiframework.testng.base.BaseApiTest;
import com.apiframework.testng.base.TestExecutionContext;
import com.apiframework.testng.retry.FrameworkRetryAnalyzer;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Label;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Test lifecycle reporting listener for Allure.
 * <p>
 * Responsible for test-level labels, retry metadata, failure stacktrace and run summary.
 * <p>
 * Not responsible for per-request HTTP step creation or request/response attachments
 * (these belong to the HTTP reporting filter layer).
 */
public final class AllureTestNgListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        attachContextLabels(result);
        attachRetryMetadata(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        attachRetryMetadata(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        attachRetryMetadata(result);
        if (result.getThrowable() != null) {
            Allure.addAttachment("Failure stacktrace", "text/plain", stackTraceOf(result.getThrowable()), ".txt");
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        Allure.addAttachment(
            "Test run summary",
            "text/plain",
            "passed=" + context.getPassedTests().size()
                + ", failed=" + context.getFailedTests().size()
                + ", skipped=" + context.getSkippedTests().size(),
            ".txt"
        );
    }

    private void attachRetryMetadata(ITestResult result) {
        Object retry = result.getAttribute(FrameworkRetryAnalyzer.RETRY_ATTEMPT_ATTRIBUTE);
        if (retry != null) {
            Allure.addAttachment("Retry metadata", "retryAttempt=" + retry);
        }
    }

    private void attachContextLabels(ITestResult result) {
        Object contextAttribute = result.getAttribute(BaseApiTest.TEST_CONTEXT_ATTRIBUTE);
        if (!(contextAttribute instanceof TestExecutionContext context)) {
            return;
        }

        Allure.label("testId", context.testId());
        Allure.label("correlationId", context.correlationId());

        for (Map.Entry<String, String> tag : context.environmentTags().entrySet()) {
            Allure.getLifecycle().updateTestCase(testResult ->
                testResult.getLabels().add(new Label().setName(tag.getKey()).setValue(tag.getValue()))
            );
        }
    }

    private String stackTraceOf(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
