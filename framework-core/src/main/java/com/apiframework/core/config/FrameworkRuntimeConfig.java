package com.apiframework.core.config;

import com.apiframework.core.model.HttpRetryPolicy;

public record FrameworkRuntimeConfig(
    EnvironmentProfile profile,
    String baseUrl,
    int connectTimeoutMs,
    int readTimeoutMs,
    HttpRetryPolicy httpRetryPolicy,
    BasicAuthConfig basicAuth,
    OAuth2Config oauth2
) {
}
