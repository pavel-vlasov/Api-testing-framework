package com.apiframework.core.filter;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.UUID;

public final class CorrelationIdFilter implements Filter {
    public static final String HEADER_NAME = "X-Correlation-Id";

    @Override
    public Response filter(
        FilterableRequestSpecification requestSpec,
        FilterableResponseSpecification responseSpec,
        FilterContext context
    ) {
        if (requestSpec.getHeaders().getValue(HEADER_NAME) == null) {
            requestSpec.header(HEADER_NAME, UUID.randomUUID().toString());
        }
        return context.next(requestSpec, responseSpec);
    }
}
