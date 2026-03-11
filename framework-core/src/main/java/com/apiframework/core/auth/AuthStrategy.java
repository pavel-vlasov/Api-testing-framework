package com.apiframework.core.auth;

import io.restassured.specification.RequestSpecification;

public interface AuthStrategy {
    void apply(RequestSpecification requestSpecification);

    default String name() {
        return getClass().getSimpleName();
    }

    static AuthStrategy none() {
        return NoAuthStrategy.INSTANCE;
    }
}
