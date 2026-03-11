package com.apiframework.core.http;

import com.apiframework.core.auth.AuthStrategy;
import com.apiframework.core.auth.RefreshableAuthStrategy;
import com.apiframework.core.filter.CorrelationIdFilter;
import com.apiframework.core.json.JacksonProvider;
import com.apiframework.core.model.ApiRequest;
import com.apiframework.core.model.ApiResponse;
import com.apiframework.core.model.HttpMethod;
import com.apiframework.core.model.HttpRetryPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class RestAssuredHttpClient implements HttpClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAssuredHttpClient.class);

    private final RequestSpecification baseSpec;
    private final AuthStrategy authStrategy;
    private final HttpRetryPolicy defaultRetryPolicy;
    private final ObjectMapper objectMapper;

    public RestAssuredHttpClient(RequestSpecification baseSpec, AuthStrategy authStrategy, HttpRetryPolicy defaultRetryPolicy) {
        this(baseSpec, authStrategy, defaultRetryPolicy, JacksonProvider.defaultMapper());
    }

    public RestAssuredHttpClient(
        RequestSpecification baseSpec,
        AuthStrategy authStrategy,
        HttpRetryPolicy defaultRetryPolicy,
        ObjectMapper objectMapper
    ) {
        this.baseSpec = baseSpec;
        this.authStrategy = authStrategy;
        this.defaultRetryPolicy = defaultRetryPolicy;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> ApiResponse<T> execute(ApiRequest<?> request, Class<T> responseType) {
        HttpRetryPolicy retryPolicy = request.retryPolicy() == null ? defaultRetryPolicy : request.retryPolicy();

        boolean refreshedAfter401 = false;
        for (int attempt = 1; attempt <= retryPolicy.maxAttempts(); attempt++) {
            Response response = executeOnce(request);
            String rawBody = response.getBody() == null ? "" : response.getBody().asString();

            if (!refreshedAfter401 && authStrategy instanceof RefreshableAuthStrategy refreshableAuth
                && refreshableAuth.shouldRefresh(response.statusCode(), rawBody)) {
                LOGGER.info("Refreshing OAuth2 token due to unauthorized response");
                refreshableAuth.refreshToken();
                refreshedAfter401 = true;
                continue;
            }

            boolean shouldRetry = attempt < retryPolicy.maxAttempts()
                && retryPolicy.retryableStatusCodes().contains(response.statusCode());

            if (shouldRetry) {
                LOGGER.warn("HTTP retry attempt {}/{} due to status {}", attempt, retryPolicy.maxAttempts(), response.statusCode());
                sleepQuietly(retryPolicy.delayBetweenAttempts().toMillis());
                continue;
            }

            return toApiResponse(response, rawBody, responseType);
        }

        throw new IllegalStateException("Exhausted HTTP retries without a final response");
    }

    private Response executeOnce(ApiRequest<?> request) {
        Objects.requireNonNull(request.method(), "HTTP method must not be null");
        String path = Objects.requireNonNull(request.path(), "Request path must not be null");

        RequestSpecification specification = RestAssured
            .given()
            .spec(new RequestSpecBuilder().addRequestSpecification(baseSpec).build());

        authStrategy.apply(specification);

        if (!request.headers().isEmpty()) {
            specification.headers(request.headers());
        }

        if (!request.queryParams().isEmpty()) {
            specification.queryParams(request.queryParams());
        }

        if (request.body() != null) {
            specification.body(request.body());
        }

        return switch (request.method()) {
            case GET -> specification.get(path);
            case POST -> specification.post(path);
            case PUT -> specification.put(path);
            case PATCH -> specification.patch(path);
            case DELETE -> specification.delete(path);
        };
    }

    private <T> ApiResponse<T> toApiResponse(Response response, String rawBody, Class<T> responseType) {
        T body = deserializeBody(rawBody, responseType);
        Map<String, String> headers = new LinkedHashMap<>();
        response.getHeaders().asList().forEach(header -> headers.put(header.getName(), header.getValue()));
        String correlationId = headers.get(CorrelationIdFilter.HEADER_NAME);

        return new ApiResponse<>(
            response.statusCode(),
            headers,
            body,
            response.time(),
            correlationId,
            rawBody
        );
    }

    private <T> T deserializeBody(String rawBody, Class<T> responseType) {
        if (responseType == String.class) {
            return responseType.cast(rawBody);
        }
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(rawBody, responseType);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to deserialize response body", exception);
        }
    }

    private void sleepQuietly(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry sleep interrupted", interruptedException);
        }
    }
}
