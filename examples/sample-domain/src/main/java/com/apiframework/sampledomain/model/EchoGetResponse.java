package com.apiframework.sampledomain.model;

import java.util.Map;

public record EchoGetResponse(
    Map<String, String> args,
    Map<String, String> headers,
    String url
) {
}
