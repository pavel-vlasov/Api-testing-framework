package com.apiframework.messaging;

import com.apiframework.messaging.model.ConsumedMessage;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public interface MessageBusClient extends AutoCloseable {
    void publish(String exchange, String routingKey, String payload, String correlationId, Map<String, Object> headers);

    Optional<ConsumedMessage> consumeOne(String queue, Duration timeout);

    @Override
    void close();
}
