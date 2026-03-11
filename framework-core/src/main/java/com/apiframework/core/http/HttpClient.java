package com.apiframework.core.http;

import com.apiframework.core.model.ApiRequest;
import com.apiframework.core.model.ApiResponse;

public interface HttpClient {
    <T> ApiResponse<T> execute(ApiRequest<?> request, Class<T> responseType);

    default ApiResponse<String> executeRaw(ApiRequest<?> request) {
        return execute(request, String.class);
    }
}
