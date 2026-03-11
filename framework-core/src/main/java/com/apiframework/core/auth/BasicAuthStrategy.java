package com.apiframework.core.auth;

import io.restassured.specification.RequestSpecification;

import java.util.Objects;

public final class BasicAuthStrategy implements AuthStrategy {
    private final String username;
    private final String password;

    public BasicAuthStrategy(String username, String password) {
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.password = Objects.requireNonNull(password, "password must not be null");
    }

    @Override
    public void apply(RequestSpecification requestSpecification) {
        requestSpecification.auth().preemptive().basic(username, password);
    }
}
