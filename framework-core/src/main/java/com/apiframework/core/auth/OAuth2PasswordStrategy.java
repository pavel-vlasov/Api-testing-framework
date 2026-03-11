package com.apiframework.core.auth;

import com.apiframework.core.auth.oauth.AbstractOAuth2Strategy;
import com.apiframework.core.auth.oauth.OAuth2TokenManager;
import com.apiframework.core.auth.oauth.PasswordTokenFetcher;

import java.time.Clock;
import java.time.Duration;

public final class OAuth2PasswordStrategy extends AbstractOAuth2Strategy {

    public OAuth2PasswordStrategy(
        String tokenUrl,
        String clientId,
        String clientSecret,
        String username,
        String password,
        String scope,
        Duration refreshSkew
    ) {
        super(new OAuth2TokenManager(
            new PasswordTokenFetcher(tokenUrl, clientId, clientSecret, username, password, scope),
            refreshSkew,
            Clock.systemUTC()
        ));
    }
}
