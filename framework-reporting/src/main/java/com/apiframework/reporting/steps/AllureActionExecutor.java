package com.apiframework.reporting.steps;

import io.qameta.allure.Allure;

import java.util.function.Supplier;

/**
 * Executor for business and test semantics in Allure steps.
 * <p>
 * Responsible for creating reusable Flow/Action/Assert steps in flow and assertion layers.
 * <p>
 * Not responsible for transport-level request/response attachments (HTTP filters layer)
 * and not responsible for TestNG lifecycle metadata/summary (listener layer).
 */
public final class AllureActionExecutor {
    public <T> T action(String name, Supplier<T> action) {
        return Allure.step("Action: " + name, action::get);
    }

    public void action(String name, Runnable action) {
        Allure.step("Action: " + name, action::run);
    }

    public <T> T assertion(String name, Supplier<T> assertion) {
        return Allure.step("Assert: " + name, assertion::get);
    }

    public void assertion(String name, Runnable assertion) {
        Allure.step("Assert: " + name, assertion::run);
    }

    public <T> T composite(String name, Supplier<T> flow) {
        return Allure.step("Flow: " + name, flow::get);
    }

    public void composite(String name, Runnable flow) {
        Allure.step("Flow: " + name, flow::run);
    }
}
