package com.apiframework.reporting.allure;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.StepResult;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.UUID;

/**
 * Transport-level Allure reporting filter for a single HTTP request/response exchange.
 * <p>
 * Responsible for HTTP step lifecycle, request/response/metadata attachments, masking, and
 * transport error attachment.
 * <p>
 * Not responsible for business-level Flow/Action/Assert semantics (see AllureActionExecutor)
 * or TestNG lifecycle reporting such as labels/retry/summary (see AllureTestNgListener).
 */
public final class AllureHttpStepFilter implements Filter {
    private final HttpStepNameStrategy stepNameStrategy;
    private final HttpAttachmentRenderer attachmentRenderer;
    private final HttpErrorAttachmentStrategy errorAttachmentStrategy;

    public AllureHttpStepFilter(boolean attachmentsEnabled) {
        this(HttpReportingConfiguration.defaultConfiguration(attachmentsEnabled));
    }

    public AllureHttpStepFilter(HttpReportingConfiguration configuration) {
        this.stepNameStrategy = configuration.stepNameStrategy();
        this.attachmentRenderer = configuration.attachmentRenderer();
        this.errorAttachmentStrategy = configuration.errorAttachmentStrategy();
    }

    @Override
    public Response filter(
        FilterableRequestSpecification requestSpec,
        FilterableResponseSpecification responseSpec,
        FilterContext context
    ) {
        String stepId = UUID.randomUUID().toString();
        Allure.getLifecycle().startStep(stepId, new StepResult().setName(stepNameStrategy.beforeCall(requestSpec)));

        long startedAtNanos = System.nanoTime();
        try {
            attachmentRenderer.attachRequest(requestSpec);

            Response response = context.next(requestSpec, responseSpec);
            long durationMs = elapsedMs(startedAtNanos);

            Allure.getLifecycle().updateStep(stepId, step -> {
                step.setName(stepNameStrategy.afterCall(requestSpec, response));
                step.setStatus(resolveStatus(response.getStatusCode()));
            });

            attachmentRenderer.attachResponse(requestSpec, response, durationMs);
            return response;
        } catch (Throwable throwable) {
            long durationMs = elapsedMs(startedAtNanos);
            errorAttachmentStrategy.attachError(requestSpec, throwable, durationMs);
            Allure.getLifecycle().updateStep(stepId, step -> {
                step.setStatus(Status.BROKEN);
                step.setStatusDetails(new StatusDetails().setMessage(throwable.getMessage()));
            });
            throw throwable;
        } finally {
            Allure.getLifecycle().stopStep(stepId);
        }
    }

    private long elapsedMs(long startedAtNanos) {
        return (System.nanoTime() - startedAtNanos) / 1_000_000;
    }

    private Status resolveStatus(int statusCode) {
        if (statusCode >= 500) {
            return Status.BROKEN;
        }
        if (statusCode >= 400) {
            return Status.FAILED;
        }
        return Status.PASSED;
    }
}
