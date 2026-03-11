package com.apiframework.sampledomain.endpoint;

import com.apiframework.core.http.HttpClient;
import com.apiframework.core.model.ApiRequest;
import com.apiframework.core.model.ApiResponse;
import com.apiframework.core.model.HttpMethod;
import com.apiframework.sampledomain.model.EchoGetResponse;
import com.apiframework.sampledomain.model.EchoPayload;
import com.apiframework.sampledomain.model.EchoPostResponse;

import java.util.Objects;

public final class PostmanEchoApi {
    private final HttpClient httpClient;

    public PostmanEchoApi(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient must not be null");
    }

    public ApiResponse<EchoGetResponse> getEcho(String key, String value) {
        ApiRequest<Void> request = ApiRequest.<Void>builder(HttpMethod.GET, PostmanEchoRoute.GET_ECHO.path())
            .queryParam(key, value)
            .build();
        return httpClient.execute(request, EchoGetResponse.class);
    }

    public ApiResponse<EchoPostResponse> postEcho(EchoPayload payload) {
        ApiRequest<EchoPayload> request = ApiRequest.<EchoPayload>builder(HttpMethod.POST, PostmanEchoRoute.POST_ECHO.path())
            .body(payload)
            .build();
        return httpClient.execute(request, EchoPostResponse.class);
    }
}
