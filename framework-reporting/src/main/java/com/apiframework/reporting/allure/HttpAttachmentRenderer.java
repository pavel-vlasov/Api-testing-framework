package com.apiframework.reporting.allure;

import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;

public interface HttpAttachmentRenderer {
    void attachRequest(FilterableRequestSpecification requestSpec);

    void attachResponse(FilterableRequestSpecification requestSpec, Response response, long durationMs);
}
