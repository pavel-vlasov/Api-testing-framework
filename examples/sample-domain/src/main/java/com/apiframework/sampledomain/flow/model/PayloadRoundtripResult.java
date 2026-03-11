package com.apiframework.sampledomain.flow.model;

import com.apiframework.core.model.ApiResponse;
import com.apiframework.sampledomain.model.EchoPayload;
import com.apiframework.sampledomain.model.EchoPostResponse;

import java.util.Objects;

public final class PayloadRoundtripResult {
    private final EchoPayload payload;
    private final ApiResponse<EchoPostResponse> response;

    public PayloadRoundtripResult(EchoPayload payload, ApiResponse<EchoPostResponse> response) {
        this.payload = Objects.requireNonNull(payload, "payload must not be null");
        this.response = Objects.requireNonNull(response, "response must not be null");
    }

    public EchoPayload payload() {
        return payload;
    }

    public ApiResponse<EchoPostResponse> response() {
        return response;
    }
}
