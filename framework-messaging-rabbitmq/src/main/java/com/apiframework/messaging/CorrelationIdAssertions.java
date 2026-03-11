package com.apiframework.messaging;

import com.apiframework.messaging.model.ConsumedMessage;

public final class CorrelationIdAssertions {
    private CorrelationIdAssertions() {
    }

    public static void assertCorrelationId(ConsumedMessage message, String expectedCorrelationId) {
        if (message == null) {
            throw new AssertionError("Message is null");
        }
        if (!expectedCorrelationId.equals(message.correlationId())) {
            throw new AssertionError(
                "Unexpected correlation id. expected=" + expectedCorrelationId + ", actual=" + message.correlationId()
            );
        }
    }
}
