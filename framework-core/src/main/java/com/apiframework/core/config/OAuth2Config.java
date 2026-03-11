package com.apiframework.core.config;

import java.time.Duration;

public record OAuth2Config(
    String tokenUrl,
    String clientId,
    String clientSecret,
    String username,
    String password,
    String scope,
    Duration refreshSkew
) {
}
