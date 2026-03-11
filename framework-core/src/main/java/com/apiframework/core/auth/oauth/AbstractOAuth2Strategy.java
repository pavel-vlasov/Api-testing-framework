package com.apiframework.core.auth.oauth;

import com.apiframework.core.auth.RefreshableAuthStrategy;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractOAuth2Strategy implements RefreshableAuthStrategy {
    private final OAuth2TokenManager tokenManager;

    protected AbstractOAuth2Strategy(OAuth2TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public void apply(RequestSpecification requestSpecification) {
        requestSpecification.auth().oauth2(tokenManager.getValidAccessToken());
    }

    @Override
    public boolean shouldRefresh(int statusCode, String responseBody) {
        return statusCode == 401 && (responseBody == null || responseBody.contains("invalid_token"));
    }

    @Override
    public void refreshToken() {
        tokenManager.forceRefresh();
    }
}
