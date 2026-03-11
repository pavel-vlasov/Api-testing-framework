package com.apiframework.core.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ApiRequest<T> {
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> headers;
    private final Map<String, Object> queryParams;
    private final T body;
    private final HttpRetryPolicy retryPolicy;

    private ApiRequest(Builder<T> builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.headers = Collections.unmodifiableMap(new LinkedHashMap<>(builder.headers));
        this.queryParams = Collections.unmodifiableMap(new LinkedHashMap<>(builder.queryParams));
        this.body = builder.body;
        this.retryPolicy = builder.retryPolicy;
    }

    public HttpMethod method() {
        return method;
    }

    public String path() {
        return path;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Map<String, Object> queryParams() {
        return queryParams;
    }

    public T body() {
        return body;
    }

    public HttpRetryPolicy retryPolicy() {
        return retryPolicy;
    }

    public static <T> Builder<T> builder(HttpMethod method, String path) {
        return new Builder<>(method, path);
    }

    public static final class Builder<T> {
        private final HttpMethod method;
        private final String path;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final Map<String, Object> queryParams = new LinkedHashMap<>();
        private T body;
        private HttpRetryPolicy retryPolicy;

        public Builder(HttpMethod method, String path) {
            this.method = method;
            this.path = path;
        }

        public Builder<T> header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        public Builder<T> headers(Map<String, String> newHeaders) {
            headers.putAll(newHeaders);
            return this;
        }

        public Builder<T> queryParam(String key, Object value) {
            queryParams.put(key, value);
            return this;
        }

        public Builder<T> queryParams(Map<String, Object> params) {
            queryParams.putAll(params);
            return this;
        }

        public Builder<T> body(T requestBody) {
            body = requestBody;
            return this;
        }

        public Builder<T> retryPolicy(HttpRetryPolicy policy) {
            retryPolicy = policy;
            return this;
        }

        public ApiRequest<T> build() {
            return new ApiRequest<>(this);
        }
    }
}
