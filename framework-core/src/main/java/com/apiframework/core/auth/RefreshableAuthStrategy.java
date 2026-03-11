package com.apiframework.core.auth;

public interface RefreshableAuthStrategy extends AuthStrategy {
    boolean shouldRefresh(int statusCode, String responseBody);

    void refreshToken();
}
