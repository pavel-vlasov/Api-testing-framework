package com.apiframework.messaging;

import com.apiframework.messaging.model.ConsumedMessage;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public final class MessageAwaiter {
    private final MessageBusClient messageBusClient;

    public MessageAwaiter(MessageBusClient messageBusClient) {
        this.messageBusClient = messageBusClient;
    }

    public ConsumedMessage awaitMessage(
        String queue,
        Duration timeout,
        Duration pollInterval,
        Predicate<ConsumedMessage> predicate
    ) {
        AtomicReference<ConsumedMessage> captured = new AtomicReference<>();

        Awaitility.await("Wait for message")
            .atMost(timeout)
            .pollInterval(pollInterval)
            .until(() -> {
                return messageBusClient.consumeOne(queue, pollInterval)
                    .filter(predicate)
                    .map(message -> {
                        captured.set(message);
                        return true;
                    })
                    .orElse(false);
            });

        return captured.get();
    }
}
