package com.apiframework.sampledomain.endpoint;

import com.apiframework.core.http.HttpClient;

@Deprecated
public final class UserApi extends BaseApiEndpoint {
    public UserApi(HttpClient httpClient) {
        super(httpClient);
    }
}
