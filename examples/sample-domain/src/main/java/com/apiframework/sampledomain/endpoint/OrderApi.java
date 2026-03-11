package com.apiframework.sampledomain.endpoint;

import com.apiframework.core.http.HttpClient;

@Deprecated
public final class OrderApi extends BaseApiEndpoint {
    public OrderApi(HttpClient httpClient) {
        super(httpClient);
    }
}
