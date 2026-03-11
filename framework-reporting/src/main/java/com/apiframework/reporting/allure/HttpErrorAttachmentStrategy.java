package com.apiframework.reporting.allure;

import io.restassured.specification.FilterableRequestSpecification;

public interface HttpErrorAttachmentStrategy {
    void attachError(FilterableRequestSpecification requestSpec, Throwable throwable, long durationMs);
}
