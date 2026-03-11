package com.apiframework.core.auth;

import com.apiframework.core.auth.oauth.AbstractOAuth2Strategy;
import com.apiframework.core.auth.oauth.ClientCredentialsTokenFetcher;
import com.apiframework.core.auth.oauth.OAuth2TokenManager;

import java.time.Clock;
import java.time.Duration;

public final class OAuth2ClientCredentialsStrategy extends AbstractOAuth2Strategy {

    public OAuth2ClientCredentialsStrategy(
        String tokenUrl,
        String clientId,
        String clientSecret,
        String scope,
        Duration refreshSkew
    ) {
        super(new OAuth2TokenManager(
            new ClientCredentialsTokenFetcher(tokenUrl, clientId, clientSecret, scope),
            refreshSkew,
            Clock.systemUTC()
        ));
    }
}
