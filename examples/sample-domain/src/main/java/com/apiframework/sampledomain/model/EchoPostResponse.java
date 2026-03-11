package com.apiframework.sampledomain.model;

import java.util.Map;

public record EchoPostResponse(
    String data,
    EchoPayload json,
    Map<String, String> headers,
    String url
) {
}
