package com.apiframework.reporting.allure;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Backward-compatible wrapper.
 */
public final class AllureRequestResponseFilter implements Filter {
    private final AllureHttpStepFilter delegate = new AllureHttpStepFilter(true);

    @Override
    public Response filter(
        FilterableRequestSpecification requestSpec,
        FilterableResponseSpecification responseSpec,
        FilterContext context
    ) {
        return delegate.filter(requestSpec, responseSpec, context);
    }
}
