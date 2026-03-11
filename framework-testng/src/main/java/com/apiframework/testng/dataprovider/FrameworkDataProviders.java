package com.apiframework.testng.dataprovider;

import org.testng.annotations.DataProvider;

public final class FrameworkDataProviders {
    private FrameworkDataProviders() {
    }

    @DataProvider(name = "sample-users")
    public static Object[][] sampleUsers() {
        return new Object[][]{
            {"qa+smoke1@example.com", "John", "Smoke"},
            {"qa+smoke2@example.com", "Alice", "Test"}
        };
    }
}
