package com.apiframework.core.auth;

import io.restassured.specification.RequestSpecification;

enum NoAuthStrategy implements AuthStrategy {
    INSTANCE;

    @Override
    public void apply(RequestSpecification requestSpecification) {
        // Explicit no-op strategy to keep API client wiring consistent.
    }
}
