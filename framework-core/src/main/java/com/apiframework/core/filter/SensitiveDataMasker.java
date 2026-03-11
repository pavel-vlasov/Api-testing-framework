package com.apiframework.core.filter;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SensitiveDataMasker {
    private static final String MASK = "***";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
        "authorization",
        "proxy-authorization",
        "cookie",
        "set-cookie",
        "password",
        "access_token",
        "refresh_token",
        "secret",
        "client_secret",
        "api_key",
        "token"
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
        "(?i)(\\\"(?:password|token|secret|authorization|access_token|refresh_token|client_secret|api_key)\\\"\\s*:\\s*\\\")(.*?)(\\\")"
    );

    private SensitiveDataMasker() {
    }

    public static Map<String, String> maskHeaders(Map<String, String> headers) {
        return headers.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> isSensitive(entry.getKey()) ? MASK : entry.getValue(),
                (left, right) -> right
            ));
    }

    public static String maskJsonLikeBody(String body) {
        if (body == null || body.isBlank()) {
            return body;
        }

        String maskedJsonPairs = TOKEN_PATTERN.matcher(body).replaceAll("$1" + MASK + "$3");
        return maskedJsonPairs
            .replaceAll("(?i)Bearer\\s+[A-Za-z0-9._\\-]+", "Bearer " + MASK)
            .replaceAll("(?i)(api[_-]?key=)([^&\\s]+)", "$1" + MASK)
            .replaceAll("(?i)(token=)([^&\\s]+)", "$1" + MASK);
    }

    private static boolean isSensitive(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return SENSITIVE_KEYS.contains(normalized)
            || normalized.contains("token")
            || normalized.contains("secret")
            || normalized.contains("authorization")
            || normalized.contains("cookie");
    }
}
