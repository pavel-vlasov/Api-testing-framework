package com.apiframework.sampledomain.model;

public record EchoPayload(
    String event,
    int amount,
    boolean active
) {
}
