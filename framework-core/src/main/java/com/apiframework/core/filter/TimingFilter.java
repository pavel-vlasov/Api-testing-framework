package com.apiframework.core.filter;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TimingFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TimingFilter.class);

    @Override
    public Response filter(
        FilterableRequestSpecification requestSpec,
        FilterableResponseSpecification responseSpec,
        FilterContext context
    ) {
        long start = System.nanoTime();
        Response response = context.next(requestSpec, responseSpec);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        LOGGER.info("HTTP {} {} -> {} in {} ms",
            requestSpec.getMethod(),
            requestSpec.getURI(),
            response.getStatusCode(),
            elapsedMs);
        return response;
    }
}
