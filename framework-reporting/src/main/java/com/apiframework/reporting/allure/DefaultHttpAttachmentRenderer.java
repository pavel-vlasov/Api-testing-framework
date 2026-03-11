package com.apiframework.reporting.allure;

import com.apiframework.core.filter.CorrelationIdFilter;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public final class DefaultHttpAttachmentRenderer implements HttpAttachmentRenderer {
    private final boolean attachmentsEnabled;
    private final int maxAttachmentBytes;
    private final HttpMaskingStrategy maskingStrategy;

    public DefaultHttpAttachmentRenderer(boolean attachmentsEnabled, int maxAttachmentBytes, HttpMaskingStrategy maskingStrategy) {
        this.attachmentsEnabled = attachmentsEnabled;
        this.maxAttachmentBytes = maxAttachmentBytes;
        this.maskingStrategy = maskingStrategy;
    }

    @Override
    public void attachRequest(FilterableRequestSpecification requestSpec) {
        if (!attachmentsEnabled) {
            return;
        }
        Map<String, String> headers = requestSpec.getHeaders().asList().stream()
            .collect(Collectors.toMap(h -> h.getName(), h -> h.getValue(), (l, r) -> r, LinkedHashMap::new));

        String content = "method=" + requestSpec.getMethod() + "\n"
            + "path=" + pathFromUri(requestSpec.getURI()) + "\n"
            + "headers=" + maskingStrategy.maskHeaders(headers) + "\n"
            + "body=" + truncate(maskingStrategy.maskBody(safeRequestBody(requestSpec))) + "\n";
        Allure.addAttachment("HTTP Request", "text/plain", content, ".txt");
        Allure.addAttachment("cURL Preview", "text/plain", buildCurlPreview(requestSpec), ".txt");
    }

    @Override
    public void attachResponse(FilterableRequestSpecification requestSpec, Response response, long durationMs) {
        if (!attachmentsEnabled) {
            return;
        }

        Map<String, String> headers = response.getHeaders().asList().stream()
            .collect(Collectors.toMap(h -> h.getName(), h -> h.getValue(), (l, r) -> r, LinkedHashMap::new));
        String body = response.getBody() == null ? "" : response.getBody().asString();

        String responseContent = "status=" + response.getStatusCode() + "\n"
            + "headers=" + maskingStrategy.maskHeaders(headers) + "\n"
            + "body=" + truncate(maskingStrategy.maskBody(body)) + "\n";
        Allure.addAttachment("HTTP Response", "text/plain", responseContent, ".txt");

        String metadata = "method=" + requestSpec.getMethod() + "\n"
            + "path=" + pathFromUri(requestSpec.getURI()) + "\n"
            + "statusCode=" + response.getStatusCode() + "\n"
            + "durationMs=" + durationMs + "\n"
            + "correlationId=" + requestSpec.getHeaders().getValue(CorrelationIdFilter.HEADER_NAME) + "\n"
            + "contentType=" + response.getContentType() + "\n"
            + "profile=" + System.getProperty("framework.profile", "<unknown>") + "\n"
            + "environment=" + System.getProperty("framework.environment", "<unknown>") + "\n";
        Allure.addAttachment("HTTP Metadata", "text/plain", metadata, ".txt");

        if (response.getStatusCode() >= 400) {
            Allure.addAttachment("Error Summary", "text/plain", "status=" + response.getStatusCode(), ".txt");
        }

        if (isJson(response.getContentType()) && response.getBody() != null) {
            String pretty = response.getBody().prettyPrint();
            Allure.addAttachment("Pretty JSON Response", "application/json", truncate(maskingStrategy.maskBody(pretty)), ".json");
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxAttachmentBytes) {
            return value;
        }
        return value.substring(0, maxAttachmentBytes) + "\n...<truncated>";
    }

    private boolean isJson(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("json");
    }

    private String safeRequestBody(FilterableRequestSpecification requestSpec) {
        Object body = requestSpec.getBody();
        return body == null ? "" : String.valueOf(body);
    }

    private String buildCurlPreview(FilterableRequestSpecification requestSpec) {
        StringBuilder builder = new StringBuilder("curl -X ")
            .append(requestSpec.getMethod())
            .append(" '")
            .append(requestSpec.getURI())
            .append("'");
        requestSpec.getHeaders().asList().forEach(header -> builder.append(" -H '")
            .append(header.getName())
            .append(": ")
            .append(maskingStrategy.maskBody(header.getValue()))
            .append("'"));
        String body = safeRequestBody(requestSpec);
        if (!body.isBlank()) {
            builder.append(" --data '").append(truncate(maskingStrategy.maskBody(body))).append("'");
        }
        return builder.append('\n').toString();
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
