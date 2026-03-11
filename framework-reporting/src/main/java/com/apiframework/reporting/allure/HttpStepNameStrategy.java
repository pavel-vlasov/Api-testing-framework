package com.apiframework.reporting.allure;

import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;

public interface HttpStepNameStrategy {
    String beforeCall(FilterableRequestSpecification requestSpec);

    String afterCall(FilterableRequestSpecification requestSpec, Response response);
}
