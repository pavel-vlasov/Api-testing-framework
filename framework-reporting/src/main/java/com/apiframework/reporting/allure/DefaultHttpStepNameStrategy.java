package com.apiframework.reporting.allure;

import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;

import java.net.URI;

public final class DefaultHttpStepNameStrategy implements HttpStepNameStrategy {
    @Override
    public String beforeCall(FilterableRequestSpecification requestSpec) {
        return requestSpec.getMethod() + " " + pathFromUri(requestSpec.getURI()) + " -> ...";
    }

    @Override
    public String afterCall(FilterableRequestSpecification requestSpec, Response response) {
        return requestSpec.getMethod() + " " + pathFromUri(requestSpec.getURI()) + " -> " + response.getStatusCode();
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
