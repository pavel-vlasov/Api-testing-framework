package com.apiframework.reporting.allure;

import io.qameta.allure.Allure;
import io.restassured.specification.FilterableRequestSpecification;

import java.net.URI;

public final class DefaultHttpErrorAttachmentStrategy implements HttpErrorAttachmentStrategy {
    @Override
    public void attachError(FilterableRequestSpecification requestSpec, Throwable throwable, long durationMs) {
        String content = "method=" + requestSpec.getMethod() + "\n"
            + "path=" + pathFromUri(requestSpec.getURI()) + "\n"
            + "durationMs=" + durationMs + "\n"
            + "error=" + throwable + "\n";
        Allure.addAttachment("HTTP Metadata", "text/plain", content, ".txt");
        Allure.addAttachment("Error Summary", "text/plain", throwable.toString(), ".txt");
    }

    private String pathFromUri(String uri) {
        try {
            URI parsed = new URI(uri);
            String path = parsed.getPath();
            return (path == null || path.isBlank()) ? "/" : path;
        } catch (Exception ignored) {
            return uri;
        }
    }
}
