package com.apiframework.sampledomain.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public record EchoPostResponse(
    JsonNode data,
    EchoPayload json,
    Map<String, String> headers,
    String url
) {
}
