package com.apiframework.core.auth.oauth;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public record OAuthToken(String accessToken, Instant expiresAt) {

    public boolean isExpired(Clock clock, Duration skew) {
        Instant now = clock.instant();
        return expiresAt.minus(skew).isBefore(now);
    }
}
