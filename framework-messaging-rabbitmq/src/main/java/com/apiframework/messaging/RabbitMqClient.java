package com.apiframework.messaging;

import com.apiframework.messaging.config.RabbitMqConnectionConfig;
import com.apiframework.messaging.model.ConsumedMessage;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public final class RabbitMqClient implements MessageBusClient {
    private final Connection connection;
    private final Channel channel;

    public RabbitMqClient(RabbitMqConnectionConfig config) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(config.host());
            factory.setPort(config.port());
            factory.setVirtualHost(config.virtualHost());
            factory.setUsername(config.username());
            factory.setPassword(config.password());
            this.connection = factory.newConnection("api-test-framework");
            this.channel = connection.createChannel();
        } catch (IOException | TimeoutException exception) {
            throw new IllegalStateException("Unable to initialize RabbitMQ client", exception);
        }
    }

    @Override
    public void publish(String exchange, String routingKey, String payload, String correlationId, Map<String, Object> headers) {
        try {
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .correlationId(correlationId)
                .headers(headers)
                .contentType("application/json")
                .build();
            channel.basicPublish(exchange, routingKey, properties, payload.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to publish message", exception);
        }
    }

    @Override
    public Optional<ConsumedMessage> consumeOne(String queue, Duration timeout) {
        long deadlineMs = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadlineMs) {
            try {
                GetResponse response = channel.basicGet(queue, true);
                if (response != null) {
                    String correlationId = response.getProps().getCorrelationId();
                    String payload = new String(response.getBody(), StandardCharsets.UTF_8);
                    Map<String, Object> headers = response.getProps().getHeaders() == null
                        ? Collections.emptyMap()
                        : response.getProps().getHeaders();
                    return Optional.of(new ConsumedMessage(correlationId, payload, headers));
                }
                Thread.sleep(200);
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to consume message", exception);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Consume polling interrupted", interruptedException);
            }
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        try {
            if (channel.isOpen()) {
                channel.close();
            }
            if (connection.isOpen()) {
                connection.close();
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to close RabbitMQ resources", exception);
        }
    }
}
