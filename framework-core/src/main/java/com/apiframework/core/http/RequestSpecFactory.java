package com.apiframework.core.http;

import com.apiframework.core.config.FrameworkRuntimeConfig;
import com.apiframework.core.filter.HttpFilterPolicy;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class RequestSpecFactory {
    private final FrameworkRuntimeConfig config;
    private final HttpFilterPolicy filterPolicy;

    public RequestSpecFactory(FrameworkRuntimeConfig config, HttpFilterPolicy filterPolicy) {
        this.config = config;
        this.filterPolicy = filterPolicy;
    }

    public RequestSpecification createBaseSpec() {
        RestAssuredConfig restAssuredConfig = RestAssuredConfig.config().httpClient(
            HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", config.connectTimeoutMs())
                .setParam("http.socket.timeout", config.readTimeoutMs())
        );

        RequestSpecBuilder builder = new RequestSpecBuilder()
            .setBaseUri(config.baseUrl())
            .setContentType(ContentType.JSON)
            .setConfig(restAssuredConfig);

        for (Filter filter : filterPolicy.filters()) {
            builder.addFilter(filter);
        }

        return builder.build();
    }
}
