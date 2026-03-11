package com.apiframework.sampledomain.endpoint;

import com.apiframework.core.http.HttpClient;

public abstract class BaseApiEndpoint {
    protected final HttpClient httpClient;

    protected BaseApiEndpoint(HttpClient httpClient) {
        this.httpClient = httpClient;
    }
}
