package com.apiframework.messaging.model;

import java.util.Map;

public record ConsumedMessage(String correlationId, String payload, Map<String, Object> headers) {
}
