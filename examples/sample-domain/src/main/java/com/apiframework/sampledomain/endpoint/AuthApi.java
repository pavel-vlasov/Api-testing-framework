package com.apiframework.sampledomain.endpoint;

import com.apiframework.core.http.HttpClient;

@Deprecated
public final class AuthApi extends BaseApiEndpoint {
    public AuthApi(HttpClient httpClient) {
        super(httpClient);
    }
}
