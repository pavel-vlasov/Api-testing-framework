package com.apiframework.core.model;

import java.time.Instant;
import java.util.Map;

public record ApiError(
    String errorCode,
    String message,
    Map<String, Object> details,
    String traceId,
    Instant timestamp
) {
}
