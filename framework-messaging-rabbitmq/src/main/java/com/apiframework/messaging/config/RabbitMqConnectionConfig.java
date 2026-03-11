package com.apiframework.messaging.config;

public record RabbitMqConnectionConfig(
    String host,
    int port,
    String virtualHost,
    String username,
    String password
) {
}
