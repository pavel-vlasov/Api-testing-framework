package com.apiframework.messaging;

import com.apiframework.core.json.JacksonProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class MessagePayloadMapper {
    private final ObjectMapper objectMapper;

    public MessagePayloadMapper() {
        this.objectMapper = JacksonProvider.defaultMapper();
    }

    public <T> T map(String payload, Class<T> targetClass) {
        try {
            return objectMapper.readValue(payload, targetClass);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to map message payload", exception);
        }
    }
}
