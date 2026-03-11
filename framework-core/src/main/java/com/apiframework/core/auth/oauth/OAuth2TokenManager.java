package com.apiframework.core.auth.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public final class OAuth2TokenManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2TokenManager.class);

    private final OAuth2TokenFetcher tokenFetcher;
    private final Duration refreshSkew;
    private final Clock clock;
    private final AtomicReference<OAuthToken> cache = new AtomicReference<>();
    private final ReentrantLock refreshLock = new ReentrantLock();

    public OAuth2TokenManager(OAuth2TokenFetcher tokenFetcher, Duration refreshSkew, Clock clock) {
        this.tokenFetcher = tokenFetcher;
        this.refreshSkew = refreshSkew;
        this.clock = clock;
    }

    public String getValidAccessToken() {
        OAuthToken current = cache.get();
        if (current != null && !current.isExpired(clock, refreshSkew)) {
            return current.accessToken();
        }

        refreshLock.lock();
        try {
            OAuthToken secondCheck = cache.get();
            if (secondCheck != null && !secondCheck.isExpired(clock, refreshSkew)) {
                return secondCheck.accessToken();
            }

            OAuthToken newToken = tokenFetcher.fetchToken();
            cache.set(newToken);
            LOGGER.info("OAuth2 token fetched/updated. Expires at {}", newToken.expiresAt());
            return newToken.accessToken();
        } finally {
            refreshLock.unlock();
        }
    }

    public void forceRefresh() {
        refreshLock.lock();
        try {
            OAuthToken newToken = tokenFetcher.fetchToken();
            cache.set(newToken);
            LOGGER.info("OAuth2 token force-refreshed. Expires at {}", newToken.expiresAt());
        } finally {
            refreshLock.unlock();
        }
    }
}
