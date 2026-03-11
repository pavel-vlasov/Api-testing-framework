package com.apiframework.sampledomain.flow.model;

import com.apiframework.core.model.ApiResponse;
import com.apiframework.sampledomain.model.EchoGetResponse;

import java.util.Objects;

public final class QueryRoundtripResult {
    private final String key;
    private final String expectedValue;
    private final ApiResponse<EchoGetResponse> response;

    public QueryRoundtripResult(String key, String expectedValue, ApiResponse<EchoGetResponse> response) {
        this.key = Objects.requireNonNull(key, "key must not be null");
        this.expectedValue = Objects.requireNonNull(expectedValue, "expectedValue must not be null");
        this.response = Objects.requireNonNull(response, "response must not be null");
    }

    public String key() {
        return key;
    }

    public String expectedValue() {
        return expectedValue;
    }

    public ApiResponse<EchoGetResponse> response() {
        return response;
    }
}
